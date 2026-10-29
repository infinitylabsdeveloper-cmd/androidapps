package com.example.util

import android.content.Context
import com.example.ui.screens.QuoteItem
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * Dedicated provider for inspirational educational quotes displayed on the
 * Premium Exam Portal splash screen. Ensures a different quote is selected
 * across consecutive app sessions without immediate repetition.
 *
 * How to add more quotes:
 * 1. Code-level: Add new `QuoteItem(text = "...", author = "...")` entries to `curatedQuotes` below.
 * 2. Runtime/Custom: Call `SplashQuoteProvider.addCustomQuote(context, text, author)` or
 *    `SplashQuoteProvider.addCustomQuotes(context, list)` to persist custom quotes on the device.
 */
object SplashQuoteProvider {

    private const val PREFS_NAME = "premium_splash_quote_prefs"
    private const val KEY_LAST_QUOTE_INDEX = "last_quote_index"
    private const val KEY_SESSION_COUNT = "session_count"
    private const val KEY_CUSTOM_QUOTES_JSON = "custom_quotes_json"

    /**
     * Built-in collection of inspirational quotes covering:
     * - Indian leaders & scholars (Dr. A.P.J. Abdul Kalam, Swami Vivekananda, Dr. B.R. Ambedkar, Chanakya, etc.)
     * - Civil services & competitive exam perseverance
     * - Study discipline & time management
     *
     * Add your custom QuoteItem entries directly to this list at any time.
     */
    private val curatedQuotes: List<QuoteItem> = listOf(
        // Indian Leaders & Scholars
        QuoteItem(
            text = "Education is the most powerful weapon which you can use to change the world.",
            author = "Dr. A.P.J. Abdul Kalam"
        ),
        QuoteItem(
            text = "Dream, dream, dream. Dreams transform into thoughts and thoughts result in action.",
            author = "Dr. A.P.J. Abdul Kalam"
        ),
        QuoteItem(
            text = "If you want to shine like a sun, first burn like a sun.",
            author = "Dr. A.P.J. Abdul Kalam"
        ),
        QuoteItem(
            text = "All of us do not have equal talent. But all of us have an equal opportunity to develop our talents.",
            author = "Dr. A.P.J. Abdul Kalam"
        ),
        QuoteItem(
            text = "Arise, awake, and stop not till the goal is reached.",
            author = "Swami Vivekananda"
        ),
        QuoteItem(
            text = "Take up one idea. Make that one idea your life; dream of it, think of it, live on that idea.",
            author = "Swami Vivekananda"
        ),
        QuoteItem(
            text = "All power is within you; you can do anything and everything. Believe in that.",
            author = "Swami Vivekananda"
        ),
        QuoteItem(
            text = "Cultivation of mind should be the ultimate aim of human existence.",
            author = "Dr. B.R. Ambedkar"
        ),
        QuoteItem(
            text = "Educate, agitate, and organize; have faith in yourself and never lose hope.",
            author = "Dr. B.R. Ambedkar"
        ),
        QuoteItem(
            text = "Once you start working on something, don't be afraid of failure and don't abandon it.",
            author = "Chanakya"
        ),
        QuoteItem(
            text = "A person should not be too honest. Straight trees are cut first and honest people are screwed first. Be wise and disciplined.",
            author = "Chanakya"
        ),
        QuoteItem(
            text = "Drop by drop is the water pot filled. Likewise, the wise student, gathering it little by little, fills oneself with knowledge.",
            author = "Gautama Buddha"
        ),
        QuoteItem(
            text = "You cannot cross the sea merely by standing and staring at the water.",
            author = "Rabindranath Tagore"
        ),
        QuoteItem(
            text = "Even if we lose the wealth of thousands, and our life is sacrificed, we should keep smiling and be cheerful keeping our faith in truth and discipline.",
            author = "Sardar Vallabhbhai Patel"
        ),
        QuoteItem(
            text = "Live as if you were to die tomorrow. Learn as if you were to live forever.",
            author = "Mahatma Gandhi"
        ),
        QuoteItem(
            text = "The end-product of education should be a free creative man, who can battle against historical circumstances and adversities.",
            author = "Dr. Sarvepalli Radhakrishnan"
        ),

        // Civil Services & Competitive Exam Perseverance
        QuoteItem(
            text = "Success in competitive exams is not about reading everything, but mastering what matters with relentless revision.",
            author = "Civil Services Wisdom"
        ),
        QuoteItem(
            text = "Every mock test mistake is a stepping stone that saves marks in the final examination hall.",
            author = "Exam Preparation Maxim"
        ),
        QuoteItem(
            text = "Perseverance is not a long race; it is many short races one after the other.",
            author = "Walter Elliot"
        ),
        QuoteItem(
            text = "Excellence is never an accident. It is always the result of high intention and sincere effort.",
            author = "Aristotle"
        ),
        QuoteItem(
            text = "The future belongs to those who believe in the beauty of their dreams and prepare for them today.",
            author = "Eleanor Roosevelt"
        ),

        // Study Discipline & Time Management
        QuoteItem(
            text = "Discipline is the refining fire by which talent becomes ability and preparation becomes triumph.",
            author = "Jim Rohn"
        ),
        QuoteItem(
            text = "Success is the sum of small efforts, repeated day in and day out with unwavering focus.",
            author = "Robert Collier"
        ),
        QuoteItem(
            text = "An investment in knowledge always pays the finest interest.",
            author = "Benjamin Franklin"
        ),
        QuoteItem(
            text = "Lost time is never found again. Guard every study hour with purpose and clarity.",
            author = "Benjamin Franklin"
        ),
        QuoteItem(
            text = "Do not wait; the time will never be 'just right.' Start where you stand, and work with whatever tools you have.",
            author = "Napoleon Hill"
        ),
        QuoteItem(
            text = "Focus on being productive instead of busy. One hour of deep, distraction-free study outweighs five hours of scattered reading.",
            author = "Tim Ferriss"
        ),

        // Additional premium education, exam preparation, discipline, and life quotes
        QuoteItem(
            text = "Small, consistent study sessions build knowledge that lasts.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Every page you study is a step toward the future you want.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Progress begins when you choose to start, even before you feel ready.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Your preparation today creates the choices you will have tomorrow.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A focused hour can be more valuable than an unfocused day.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Do not measure your journey by someone else's pace; measure your own progress.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Revision turns information into knowledge you can recall under pressure.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "A difficult question is an opportunity to discover what to learn next.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Consistency makes ordinary effort extraordinary over time.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Your future self will thank you for the effort you make today.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Read with curiosity, revise with discipline, and answer with confidence.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Every mistake reviewed carefully becomes a lesson earned.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "You do not need a perfect day; you need a purposeful next step.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A clear goal gives your daily effort direction.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Knowledge grows when you question, connect, and revisit what you learn.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "The strongest preparation is built one honest study session at a time.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Protect your attention; it is one of your most valuable resources.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A mock test is feedback, not a verdict on your ability.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Confidence grows from preparation, not from wishing.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Learn deeply enough to explain an idea in your own words.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Small improvements repeated daily become meaningful achievement.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "When motivation fades, let your routine carry you forward.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Your rank is an outcome; your habits are within your control.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Study to understand first; speed follows understanding.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "One setback cannot define a journey that you continue to build.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "The best time to correct a mistake is when you notice it.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Make your study plan realistic enough to follow and ambitious enough to matter.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A calm mind can use knowledge that panic hides.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The value of a lesson appears when you apply it.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Do not confuse being busy with making progress.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Each revision strengthens the path to faster recall.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Your effort matters most when nobody is watching.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Break a large goal into tasks you can complete today.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A question you get wrong can teach more than one you answer easily.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Keep promises to yourself; self-trust is built through action.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Deep learning takes patience, practice, and a willingness to be corrected.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Rest is part of a sustainable plan, not a failure of discipline.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Choose steady improvement over the pressure to be perfect.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A syllabus becomes manageable when you turn it into a daily plan.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The habit of returning to your goal is more powerful than a single burst of effort.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Be curious enough to ask why, not just what.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Use your results as a map for your next study session.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Every focused revision session reduces uncertainty.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Patience keeps your ambition working when results take time.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "The strongest learners are willing to begin again.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Your attention is a choice; spend it on what moves you forward.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A plan works when it meets the reality of your day.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Understand the concept, remember the facts, and practise the application.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Do not let one difficult chapter make you forget how far you have come.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "The courage to ask for help is part of learning well.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "A disciplined routine gives your dreams a practical path.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Review your errors without judging yourself; use them to improve.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "A little progress on a hard day still counts.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Knowledge becomes useful when paired with judgement and integrity.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Start with the next question, the next page, or the next ten minutes.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "The goal is not to study endlessly; it is to learn effectively.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Practice under realistic conditions so exam day feels familiar.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Do not wait for confidence before acting; let preparation build it.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Strong foundations make advanced topics easier to understand.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "A good learner is not afraid to update an old belief with better evidence.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Your routine should support your goal, not exhaust your spirit.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Focus on what you can control: preparation, effort, honesty, and response.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A wrong answer is useful when you understand why it was wrong.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The page you revise today may be the answer you recall tomorrow.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Aim for clarity before memorisation and understanding before speed.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Let your daily actions make your long-term ambition believable.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "The path to mastery is built through repetition with attention.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Measure success by the quality of your effort as well as the result.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A thoughtful break can restore the focus that long hours cannot.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Preparation is the quiet work behind confident performance.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Keep your notes simple enough to revise and clear enough to trust.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "When a topic feels difficult, divide it into smaller questions.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Your competition is not someone else's timeline; it is your own potential left unused.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Review the basics until they become reliable under pressure.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "A learner grows faster when curiosity is stronger than embarrassment.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Let setbacks inform your strategy, not decide your worth.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Consistency is choosing the useful action more often than the easy distraction.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Good preparation includes sleep, practice, review, and calm.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Use each week to learn something, improve something, and correct something.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A meaningful goal deserves a plan you can repeat.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Understanding one concept deeply can unlock many questions.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Do not skip review just because a topic feels familiar.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The ability to concentrate improves when you practise protecting your focus.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Be proud of the discipline you build, not only the milestones you reach.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Every day offers a chance to make your preparation more deliberate.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Learning is progress from confusion toward clarity.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Turn anxiety into a checklist of actions you can complete.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A strong answer begins with a clear understanding of the question.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Do the important work before distractions claim your best energy.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "The purpose of education is not only to pass tests but to widen understanding.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "A setback is information about your method, not a final judgement on you.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Practise recalling answers instead of only rereading notes.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Build a routine that works on ordinary days, not just inspired days.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Make time for reflection; it turns experience into better decisions.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Every concept mastered adds another tool to your thinking.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "A focused plan reduces the burden of deciding what to do next.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Be precise in your preparation and flexible in your approach.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The next attempt can be better when the last attempt is reviewed honestly.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Your worth is larger than any exam result; keep perspective while pursuing excellence.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Learning becomes stronger when you connect new ideas to what you already know.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Protect your goals from comparison and your time from distraction.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Do not aim merely to finish the syllabus; aim to understand its essentials.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "A steady learner turns limited time into meaningful progress.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Let curiosity begin the work and discipline finish it.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "An honest assessment today can prevent a repeated mistake tomorrow.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Strong character is shown in the choices you make when success is not immediate.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Use summaries to organise knowledge, then use questions to test it.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "A clear mind, a prepared plan, and steady effort make a powerful combination.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The ability to keep learning is an advantage in every stage of life.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Make your goals visible and your daily actions measurable.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Every finished practice set is feedback you can use to grow.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Do not let a slow start prevent a strong finish.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "The best preparation is active: recall, solve, review, and repeat.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Learn from people who challenge your thinking with evidence and kindness.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "You build resilience each time you return to meaningful work after difficulty.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Success becomes more sustainable when it is built on health and balance.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Use your strongest hours for your most demanding topics.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "The right question can open the door to deeper understanding.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Focus on progress you can repeat, not intensity you cannot sustain.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Treat time as a resource to direct, not a reason to panic.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Every revision is a chance to find and close a gap in understanding.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The result may arrive later; the benefit of disciplined learning starts now.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Be patient with the process and honest about the work it requires.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A prepared mind can turn pressure into a sequence of manageable steps.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "Education gives you more ways to understand the world and contribute to it.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "When you lose focus, return gently to the task instead of giving up the day.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "The quality of your questions shapes the depth of your learning.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Make mistakes small, lessons clear, and improvements continuous.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "A good study day ends with knowing what you learned and what comes next.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Let discipline guide you when emotions change.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "You do not need to know everything; you need a reliable method to learn.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Choose progress over excuses and reflection over regret.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "The strongest preparation combines knowledge, practice, revision, and composure.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "A thoughtful learner seeks understanding, not just approval.",
            author = "Education Wisdom"
        ),
        QuoteItem(
            text = "Your future is influenced by the small decisions you repeat.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "Give difficult topics your patience before you give them up.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Use feedback as a tool, not as a label.",
            author = "Exam Preparation Wisdom"
        ),
        QuoteItem(
            text = "The discipline you practise today can support goals beyond this examination.",
            author = "Life Wisdom"
        ),
        QuoteItem(
            text = "A goal becomes achievable when it is translated into today's next action.",
            author = "Study Wisdom"
        ),
        QuoteItem(
            text = "Keep learning, keep reviewing, and keep moving with purpose.",
            author = "Education Wisdom"
        )
    )

    @Volatile
    private var inMemoryLastIndex: Int = -1

    @Volatile
    private var inMemoryCustomQuotes: List<QuoteItem> = emptyList()

    /**
     * Returns all quotes (built-in curated quotes + any custom quotes saved on the device).
     */
    fun getAllQuotes(context: Context? = null): List<QuoteItem> {
        val custom = if (context != null) getCustomQuotes(context) else inMemoryCustomQuotes
        return curatedQuotes + custom
    }

    /**
     * Loads any custom user-supplied quotes saved in SharedPreferences.
     */
    fun getCustomQuotes(context: Context): List<QuoteItem> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_CUSTOM_QUOTES_JSON, null)
            if (jsonStr.isNullOrBlank()) {
                inMemoryCustomQuotes = emptyList()
                return emptyList()
            }
            val array = JSONArray(jsonStr)
            val result = mutableListOf<QuoteItem>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val text = obj.optString("text").trim()
                val author = obj.optString("author").trim().ifEmpty { "Inspirational Quote" }
                if (text.isNotEmpty()) {
                    result.add(QuoteItem(text = text, author = author))
                }
            }
            inMemoryCustomQuotes = result
            result
        } catch (_: Exception) {
            inMemoryCustomQuotes
        }
    }

    /**
     * Appends a single custom quote to the persistent splash screen quote rotation.
     */
    fun addCustomQuote(context: Context, text: String, author: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return
        val cleanAuthor = author.trim().ifEmpty { "Inspirational Quote" }
        val updated = getCustomQuotes(context).toMutableList()
        if (updated.none { it.text.equals(cleanText, ignoreCase = true) }) {
            updated.add(QuoteItem(text = cleanText, author = cleanAuthor))
            saveCustomQuotesInternal(context, updated)
        }
    }

    /**
     * Appends a list of custom quotes to the persistent splash screen quote rotation.
     */
    fun addCustomQuotes(context: Context, quotes: List<QuoteItem>) {
        if (quotes.isEmpty()) return
        val updated = getCustomQuotes(context).toMutableList()
        for (q in quotes) {
            val cleanText = q.text.trim()
            val cleanAuthor = q.author.trim().ifEmpty { "Inspirational Quote" }
            if (cleanText.isNotEmpty() && updated.none { it.text.equals(cleanText, ignoreCase = true) }) {
                updated.add(QuoteItem(text = cleanText, author = cleanAuthor))
            }
        }
        saveCustomQuotesInternal(context, updated)
    }

    /**
     * Replaces the custom quote list in SharedPreferences.
     */
    fun setCustomQuotes(context: Context, quotes: List<QuoteItem>) {
        val cleaned = quotes.mapNotNull { q ->
            val t = q.text.trim()
            if (t.isEmpty()) null else QuoteItem(t, q.author.trim().ifEmpty { "Inspirational Quote" })
        }
        saveCustomQuotesInternal(context, cleaned)
    }

    /**
     * Clears any runtime-added custom quotes while keeping the built-in curated quotes intact.
     */
    fun clearCustomQuotes(context: Context) {
        saveCustomQuotesInternal(context, emptyList())
    }

    private fun saveCustomQuotesInternal(context: Context, quotes: List<QuoteItem>) {
        inMemoryCustomQuotes = quotes
        try {
            val array = JSONArray()
            for (q in quotes) {
                val obj = JSONObject()
                obj.put("text", q.text)
                obj.put("author", q.author)
                array.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CUSTOM_QUOTES_JSON, array.toString())
                .apply()
        } catch (_: Exception) {
            // Ignore storage exceptions
        }
    }

    /**
     * Selects a quote for the current splash session, guaranteeing that consecutive
     * sessions never repeat the exact same quote. Persists across app restarts via SharedPreferences.
     */
    fun getQuoteForNextSession(context: Context?): Pair<Int, QuoteItem> {
        val allQuotes = getAllQuotes(context)
        val total = allQuotes.size
        if (total == 0) {
            return 0 to QuoteItem(
                text = "Knowledge empowers a brighter tomorrow.",
                author = "Premium Exam Portal"
            )
        }

        if (context == null) {
            val nextIdx = (inMemoryLastIndex + 1).mod(total)
            inMemoryLastIndex = nextIdx
            return nextIdx to allQuotes[nextIdx]
        }

        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastIndex = prefs.getInt(KEY_LAST_QUOTE_INDEX, -1)
            val sessionCount = prefs.getInt(KEY_SESSION_COUNT, 0)

            val nextIndex = if (lastIndex !in 0 until total) {
                0
            } else {
                // Step forward deterministically with occasional offset so all quotes are experienced
                val step = if (sessionCount % total == total - 1 && total > 2) 2 else 1
                (lastIndex + step) % total
            }

            val finalIndex = if (nextIndex == lastIndex && total > 1) {
                (lastIndex + 1) % total
            } else {
                nextIndex
            }

            prefs.edit()
                .putInt(KEY_LAST_QUOTE_INDEX, finalIndex)
                .putInt(KEY_SESSION_COUNT, sessionCount + 1)
                .apply()

            inMemoryLastIndex = finalIndex
            finalIndex to allQuotes[finalIndex]
        } catch (_: Exception) {
            val fallbackIndex = if (inMemoryLastIndex in 0 until total) {
                (inMemoryLastIndex + 1) % total
            } else {
                Random.nextInt(total)
            }
            inMemoryLastIndex = fallbackIndex
            fallbackIndex to allQuotes[fallbackIndex]
        }
    }
}
