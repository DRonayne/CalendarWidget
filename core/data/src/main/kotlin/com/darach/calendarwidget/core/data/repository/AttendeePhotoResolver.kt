package com.darach.calendarwidget.core.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves attendee emails -> contact photo thumbnail URIs. Soft-degrades to
 * nothing when READ_CONTACTS is not granted. Batched: one attendees query for
 * all events, one contacts query per distinct email (capped).
 */
@Singleton
class AttendeePhotoResolver
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
    ) {
        fun photosByEvent(eventIds: Collection<Long>): Map<Long, List<String>> {
            if (eventIds.isEmpty() || !hasContactsPermission()) return emptyMap()
            val emailsByEvent = attendeeEmails(eventIds)
            val distinctEmails =
                emailsByEvent.values
                    .flatten()
                    .distinct()
                    .take(MAX_LOOKUPS)
            val photoByEmail =
                distinctEmails
                    .mapNotNull { email -> photoForEmail(email)?.let { email to it } }
                    .toMap()
            return emailsByEvent
                .mapValues { (_, emails) ->
                    emails.mapNotNull(photoByEmail::get).distinct().take(MAX_PER_EVENT)
                }.filterValues { it.isNotEmpty() }
        }

        private fun attendeeEmails(eventIds: Collection<Long>): Map<Long, List<String>> {
            val result = mutableMapOf<Long, MutableList<String>>()
            contentResolver
                .query(
                    CalendarContract.Attendees.CONTENT_URI,
                    arrayOf(CalendarContract.Attendees.EVENT_ID, CalendarContract.Attendees.ATTENDEE_EMAIL),
                    "${CalendarContract.Attendees.EVENT_ID} IN (${eventIds.joinToString(",")})",
                    null,
                    null,
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val eventId = cursor.getLong(0)
                        val email = cursor.getString(1)?.takeIf { it.isNotBlank() } ?: continue
                        result.getOrPut(eventId) { mutableListOf() }.add(email)
                    }
                }
            return result
        }

        private fun photoForEmail(email: String): String? {
            contentResolver
                .query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Email.PHOTO_THUMBNAIL_URI),
                    "${ContactsContract.CommonDataKinds.Email.ADDRESS} = ?",
                    arrayOf(email),
                    null,
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        cursor.getString(0)?.let { return it }
                    }
                }
            return null
        }

        private fun hasContactsPermission(): Boolean =
            context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        private companion object {
            const val MAX_LOOKUPS = 20
            const val MAX_PER_EVENT = 3
        }
    }
