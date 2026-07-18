package com.darach.calendarwidget.widget.ui

import androidx.annotation.DrawableRes
import com.darach.calendarwidget.widget.R
import java.text.Normalizer

/**
 * Picks the timeline badge glyph from keywords in the event title, falling
 * back to a generic calendar icon.
 *
 * Matching rules:
 * - First matching category wins, so specific categories (named clubs, sports,
 *   compound phrases) sit above broader ones ("work", "meet", "clean").
 * - Keywords match on word boundaries: "cup" hits "World Cup" but not "cupcake".
 * - Each keyword also matches simple plural/possessive forms ("dogs", "exams",
 *   "valentine's"), and multi-word phrases tolerate hyphens ("check in"/"check-in").
 * - Titles are normalized before matching: accents stripped ("café" -> "cafe")
 *   and curly apostrophes straightened.
 */
internal object EventIcons {
    private val CATEGORIES: List<Pair<Int, List<String>>> =
        listOf(
            // Festivities and named-brand categories before generic words.
            R.drawable.ic_event_xmas to
                listOf(
                    "christmas",
                    "xmas",
                    "x-mas",
                    "santa claus",
                    "new year",
                    "nye",
                    "easter",
                    "halloween",
                    "thanksgiving",
                    "st patrick",
                    "paddy's day",
                    "fireworks",
                ),
            R.drawable.ic_event_game to
                listOf(
                    "gaming",
                    "esport",
                    "esports",
                    "dota",
                    "league of legends",
                    "warcraft",
                    "starcraft",
                    "nintendo",
                    "pokemon",
                    "counter-strike",
                    "counter strike",
                    "call of duty",
                    "overwatch",
                    "hearthstone",
                    "playstation",
                    "xbox",
                    "ps5",
                    "fortnite",
                    "minecraft",
                    "board game",
                    "boardgame",
                    "poker",
                    "chess",
                    "darts",
                    "quiz",
                    "trivia",
                ),
            // Specific sports before the generic trophy words.
            R.drawable.ic_event_soccer to
                listOf(
                    "football",
                    "soccer",
                    "manchester united",
                    "man utd",
                    "arsenal",
                    "liverpool",
                    "chelsea",
                    "real madrid",
                    "barcelona",
                    "bayern munich",
                    "premier league",
                    "champion league",
                    "champions league",
                    "la liga",
                    "uefa",
                ),
            R.drawable.ic_event_rugby to listOf("rugby", "six nations"),
            R.drawable.ic_event_tennis to listOf("tennis", "padel", "squash", "badminton"),
            R.drawable.ic_event_golf to listOf("golf"),
            R.drawable.ic_event_basketball to listOf("basketball", "nba"),
            R.drawable.ic_event_cricket to listOf("cricket"),
            R.drawable.ic_event_volleyball to listOf("volleyball"),
            R.drawable.ic_event_motorsport to
                listOf("f1", "grand prix", "formula 1", "formula one", "motogp", "nascar", "rally"),
            R.drawable.ic_event_ski to listOf("ski", "skiing", "snowboard", "snowboarding"),
            R.drawable.ic_event_trophy to
                listOf("champion", "championship", "tournament", "cup", "final", "playoff", "trophy", "derby"),
            // Pets outrank exercise so "dog walk" gets the paw, not the boot.
            R.drawable.ic_event_pets to
                listOf("dog", "cat", "pet", "vet", "puppy", "kitten", "grooming", "groomer"),
            // Exercise.
            R.drawable.ic_event_fitness to
                listOf("gym", "workout", "work out", "fitness", "crossfit", "weights"),
            R.drawable.ic_event_yoga to listOf("yoga", "pilates", "meditation", "mindfulness"),
            R.drawable.ic_event_run to
                listOf("run", "running", "jog", "jogging", "marathon", "parkrun", "5k", "10k"),
            R.drawable.ic_event_swim to listOf("swim", "swimming", "pool", "lido"),
            R.drawable.ic_event_bike to listOf("bike", "biking", "cycling", "cycle", "spin class"),
            R.drawable.ic_event_hike to
                listOf(
                    "team building",
                    "teambuilding",
                    "team outing",
                    "mountain",
                    "hike",
                    "hiking",
                    "trek",
                    "trail",
                    "walk",
                    "walking",
                    "ramble",
                ),
            // Transport: specific mode before the generic "trip".
            R.drawable.ic_event_flight to listOf("flight", "fly", "airport", "boarding", "layover"),
            R.drawable.ic_event_train to listOf("train", "railway", "tube", "metro", "luas"),
            R.drawable.ic_event_bus to listOf("bus", "coach trip"),
            R.drawable.ic_event_taxi to listOf("taxi", "cab", "uber", "lyft"),
            R.drawable.ic_event_boat to listOf("ferry", "boat", "sailing", "cruise", "kayak"),
            R.drawable.ic_event_car to
                listOf("car service", "car wash", "mot", "drive", "driving", "road trip", "roadtrip"),
            R.drawable.ic_event_travel to
                listOf("travel", "trip", "vacation", "beach", "holiday", "getaway", "city break", "airbnb"),
            R.drawable.ic_event_hotel to listOf("hotel", "hostel", "dorm", "check-in", "check in"),
            // People and occasions.
            R.drawable.ic_event_cake to listOf("birthday", "bday", "anniversary"),
            R.drawable.ic_event_love to
                listOf(
                    "love",
                    "wife",
                    "husband",
                    "darling",
                    "girlfriend",
                    "boyfriend",
                    "wedding",
                    "marry",
                    "date night",
                    "valentine",
                    "honeymoon",
                ),
            R.drawable.ic_event_baby to
                listOf("baby", "creche", "nursery", "childcare", "playdate", "playgroup", "toddler"),
            R.drawable.ic_event_church to
                listOf(
                    "church",
                    "mass",
                    "worship",
                    "prayer",
                    "christening",
                    "baptism",
                    "communion",
                    "funeral",
                    "wake",
                ),
            // Food and drink.
            R.drawable.ic_event_food to
                listOf(
                    "breakfast",
                    "dinner",
                    "lunch",
                    "brunch",
                    "meal",
                    "restaurant",
                    "bbq",
                    "barbecue",
                    "sushi",
                    "pizza",
                    "food",
                    "takeaway",
                    "kebab",
                    "curry",
                    "burger",
                ),
            R.drawable.ic_event_coffee to listOf("cafe", "coffee", "espresso", "latte", "flat white"),
            R.drawable.ic_event_wine to listOf("wine", "winery", "wine tasting", "vineyard"),
            R.drawable.ic_event_beer to listOf("beer", "brewery", "pint", "oktoberfest"),
            R.drawable.ic_event_bar to
                listOf("bar", "party", "drink", "drinks", "pub", "cocktail", "night out", "happy hour"),
            // Culture and pastimes.
            R.drawable.ic_event_music to
                listOf(
                    "music",
                    "concert",
                    "gig",
                    "piano",
                    "singing",
                    "guitar",
                    "orchestra",
                    "band",
                    "rehearsal",
                    "choir",
                    "festival",
                    "karaoke",
                ),
            R.drawable.ic_event_movie to listOf("movie", "cinema", "film", "premiere", "screening"),
            R.drawable.ic_event_theater to
                listOf("theatre", "theater", "opera", "ballet", "musical", "panto", "pantomime", "comedy"),
            R.drawable.ic_event_museum to listOf("museum", "exhibition", "exhibit"),
            R.drawable.ic_event_art to
                listOf("art", "painting", "gallery", "drawing", "craft", "pottery", "life drawing"),
            R.drawable.ic_event_photo to listOf("photo", "photoshoot", "photography", "camera"),
            R.drawable.ic_event_book to listOf("book club", "reading", "library", "book"),
            // Errands and appointments.
            R.drawable.ic_event_haircut to listOf("haircut", "barber", "hairdresser", "salon"),
            R.drawable.ic_event_spa to
                listOf("spa", "massage", "facial", "manicure", "pedicure", "nails", "waxing"),
            R.drawable.ic_event_pharmacy to
                listOf("pharmacy", "prescription", "medication", "vaccine", "vaccination", "jab", "booster"),
            R.drawable.ic_event_therapy to
                listOf("therapy", "therapist", "counselling", "counseling", "psychologist", "psychiatrist"),
            R.drawable.ic_event_health to
                listOf(
                    "hospital",
                    "clinic",
                    "doctor",
                    "dentist",
                    "physio",
                    "optician",
                    "hygienist",
                    "blood test",
                    "x-ray",
                    "mri",
                    "scan",
                    "surgery",
                ),
            R.drawable.ic_event_shopping to
                listOf("shop", "shopping", "mall", "supermarket", "market", "buy", "groceries", "grocery", "ikea"),
            R.drawable.ic_event_delivery to
                listOf("delivery", "courier", "parcel", "package", "shipping", "removal"),
            R.drawable.ic_event_cleaning to listOf("clean", "cleaning", "hoover", "declutter", "tidy"),
            R.drawable.ic_event_laundry to listOf("laundry", "dry cleaning"),
            R.drawable.ic_event_garden to
                listOf("garden", "gardening", "allotment", "lawn", "mow", "hedge"),
            R.drawable.ic_event_bank to
                listOf(
                    "money",
                    "rent",
                    "tax",
                    "bank",
                    "mortgage",
                    "invoice",
                    "payday",
                    "salary",
                    "bill",
                    "budget",
                    "insurance",
                    "pension",
                    "accountant",
                ),
            R.drawable.ic_event_legal to
                listOf("court", "lawyer", "solicitor", "legal", "notary", "barrister"),
            R.drawable.ic_event_vote to listOf("vote", "voting", "election", "referendum", "polling"),
            // Study and work; video calls outrank the generic "meeting"/"call".
            R.drawable.ic_event_code to
                listOf("code", "coding", "hackathon", "programming", "python", "java", "javascript", "sql"),
            R.drawable.ic_event_school to
                listOf(
                    "school",
                    "college",
                    "university",
                    "student",
                    "class",
                    "thesis",
                    "graduation",
                    "graduate",
                    "study",
                    "exam",
                    "lecture",
                    "seminar",
                    "tutorial",
                    "revision",
                ),
            R.drawable.ic_event_videocall to
                listOf("zoom", "video call", "facetime", "skype", "webinar", "hangout"),
            R.drawable.ic_event_work to
                listOf(
                    "work",
                    "job",
                    "meeting",
                    "conference",
                    "business",
                    "interview",
                    "standup",
                    "stand-up",
                    "1:1",
                    "1-1",
                    "one-on-one",
                    "sprint",
                    "retro",
                    "retrospective",
                    "deadline",
                    "shift",
                    "on-call",
                    "oncall",
                    "town hall",
                    "all hands",
                ),
            R.drawable.ic_event_person to
                listOf(
                    "meet",
                    "mom",
                    "mum",
                    "dad",
                    "kid",
                    "kids",
                    "parent",
                    "parents",
                    "grandma",
                    "granny",
                    "grandad",
                    "grandpa",
                    "nana",
                ),
            R.drawable.ic_event_call to listOf("call", "phone", "ring"),
            R.drawable.ic_event_flower to listOf("flower", "florist", "park", "picnic"),
        )

    /** "video call" also matches "video-call"; every keyword takes 's/s/es suffixes. */
    private fun keywordPattern(keyword: String): String {
        val words = keyword.split(' ').joinToString("[\\s\\-]+") { Regex.escape(it) }
        return "$words(?:'s|s|es)?"
    }

    private val RULES: List<Pair<Int, Regex>> =
        CATEGORIES.map { (icon, keywords) ->
            val pattern = keywords.joinToString("|", transform = ::keywordPattern)
            icon to Regex("\\b(?:$pattern)\\b", RegexOption.IGNORE_CASE)
        }

    private val COMBINING_MARKS = Regex("\\p{Mn}+")

    private fun normalize(title: String): String =
        Normalizer
            .normalize(title, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .replace('’', '\'')

    @DrawableRes
    fun forTitle(title: String): Int {
        val normalized = normalize(title)
        return RULES.firstOrNull { it.second.containsMatchIn(normalized) }?.first ?: R.drawable.ic_event
    }
}
