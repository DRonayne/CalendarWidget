package com.darach.calendarwidget.widget.ui

import com.darach.calendarwidget.widget.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventIconsTest {
    @Test
    fun `cup and final titles get the trophy icon`() {
        assertEquals(R.drawable.ic_event_trophy, EventIcons.forTitle("World Cup Final"))
        assertEquals(R.drawable.ic_event_trophy, EventIcons.forTitle("Snooker TOURNAMENT"))
        assertEquals(R.drawable.ic_event_trophy, EventIcons.forTitle("Semi-finals"))
    }

    @Test
    fun `specific sports outrank the generic trophy words`() {
        assertEquals(R.drawable.ic_event_soccer, EventIcons.forTitle("Five-a-side football"))
        assertEquals(R.drawable.ic_event_soccer, EventIcons.forTitle("Liverpool v Chelsea"))
        assertEquals(R.drawable.ic_event_rugby, EventIcons.forTitle("Six Nations opener"))
        assertEquals(R.drawable.ic_event_tennis, EventIcons.forTitle("Tennis tournament"))
        assertEquals(R.drawable.ic_event_motorsport, EventIcons.forTitle("F1: Grand Prix (Belgian GP)"))
    }

    @Test
    fun `birthday titles get the cake icon even when wedding-adjacent`() {
        assertEquals(R.drawable.ic_event_cake, EventIcons.forTitle("Maeve's birthday"))
        assertEquals(R.drawable.ic_event_cake, EventIcons.forTitle("Wedding anniversary"))
        assertEquals(R.drawable.ic_event_love, EventIcons.forTitle("Sarah's wedding"))
    }

    @Test
    fun `transport picks the specific mode before the generic trip`() {
        assertEquals(R.drawable.ic_event_flight, EventIcons.forTitle("Flight to Dublin"))
        assertEquals(R.drawable.ic_event_train, EventIcons.forTitle("Train to Cork"))
        assertEquals(R.drawable.ic_event_travel, EventIcons.forTitle("Trip to Cork"))
    }

    @Test
    fun `fitness titles get the fitness icon and specific sports their own`() {
        assertEquals(R.drawable.ic_event_fitness, EventIcons.forTitle("Gym with Dara"))
        assertEquals(R.drawable.ic_event_run, EventIcons.forTitle("Park Run"))
        assertEquals(R.drawable.ic_event_swim, EventIcons.forTitle("Swimming lesson"))
    }

    @Test
    fun `work out goes to fitness not work, video calls outrank meetings`() {
        assertEquals(R.drawable.ic_event_fitness, EventIcons.forTitle("Work out"))
        assertEquals(R.drawable.ic_event_work, EventIcons.forTitle("Team meeting"))
        assertEquals(R.drawable.ic_event_videocall, EventIcons.forTitle("Zoom meeting"))
    }

    @Test
    fun `pets outrank walking`() {
        assertEquals(R.drawable.ic_event_pets, EventIcons.forTitle("Dog walk"))
        assertEquals(R.drawable.ic_event_hike, EventIcons.forTitle("Walk by the river"))
    }

    @Test
    fun `plural and possessive forms match`() {
        assertEquals(R.drawable.ic_event_school, EventIcons.forTitle("Exams"))
        assertEquals(R.drawable.ic_event_love, EventIcons.forTitle("Valentine's dinner"))
        assertEquals(R.drawable.ic_event_pets, EventIcons.forTitle("Feed the cats"))
    }

    @Test
    fun `accents and curly apostrophes are normalized`() {
        assertEquals(R.drawable.ic_event_coffee, EventIcons.forTitle("Café catch-up"))
        assertEquals(R.drawable.ic_event_cake, EventIcons.forTitle("Maeve’s birthday"))
    }

    @Test
    fun `hyphenated phrase variants match`() {
        assertEquals(R.drawable.ic_event_videocall, EventIcons.forTitle("Video-call with recruiter"))
        assertEquals(R.drawable.ic_event_bar, EventIcons.forTitle("Happy-hour drinks"))
    }

    @Test
    fun `keywords match whole words only`() {
        assertEquals(R.drawable.ic_event, EventIcons.forTitle("Cupcake decorating"))
        assertEquals(R.drawable.ic_event, EventIcons.forTitle("Barbara's leaving do"))
        assertEquals(R.drawable.ic_event, EventIcons.forTitle("Catherine's thing"))
    }

    @Test
    fun `everything else falls back to the calendar icon`() {
        assertEquals(R.drawable.ic_event, EventIcons.forTitle("Sunrise"))
        assertEquals(R.drawable.ic_event, EventIcons.forTitle(""))
    }
}
