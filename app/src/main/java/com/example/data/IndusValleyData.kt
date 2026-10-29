package com.example.data

import com.example.model.Question

object IndusValleyData {

    val subtopicNames = listOf(
        "Harappan Chronology",
        "Chronology",
        "Discovery & Naming",
        "Discovery",
        "Geography",
        "Site Geography",
        "Dholavira",
        "Lothal",
        "Kalibangan",
        "Rakhigarhi",
        "Major Sites",
        "Harappa",
        "Mohenjo-daro",
        "Great Bath",
        "Architecture",
        "Urban Engineering",
        "Drainage System",
        "Street Planning",
        "Maritime Trade",
        "Trade & Economy",
        "Weights & Measures",
        "Craft Trade & Exchange",
        "Mesopotamian & Gulf Trade",
        "Art & Crafts",
        "Bead & Faience Production",
        "Dancing Girl",
        "Priest-King Figure",
        "Pottery",
        "Ornaments & Jewellery",
        "Shell Working",
        "Agriculture",
        "Agriculture & Crops",
        "Ritual & Religious Practices",
        "Religion",
        "Burials & Funerary Practices",
        "Animals & Domestication",
        "Society & Animals",
        "Water Management",
        "Urban Planning",
        "Brick Technology",
        "Metallurgy",
        "Transport Technology",
        "Technology",
        "Harappan Seals",
        "Unicorn Motif",
        "Indus Script & Inscriptions",
        "Script & Seals",
        "Engineering",
        "Administration",
        "Society",
        "Political Organization",
        "Heritage",
        "Decline",
        "Comparative",
        "Settlement Pattern",
        "Methodology",
        "Synthesis"
    )

    fun createQuestion(
        id: String,
        subtopic: String,
        question: String,
        optA: String,
        optB: String,
        optC: String,
        optD: String,
        correct: String = "A",
        explanation: String? = null
    ): Question {
        return Question(
            id = id,
            appId = "appsc-group2",
            subjectId = "subj-history",
            unitId = "unit-hist-1",
            topicId = "top-hist-1",
            contentLevel = "TOPIC",
            subtopic = subtopic,
            questionText = question,
            options = mapOf(
                "A" to optA,
                "B" to optB,
                "C" to optC,
                "D" to optD
            ),
            correctOption = correct,
            explanation = explanation,
            difficulty = "Medium",
            language = "English",
            isActive = true
        )
    }

    fun getQuestions(): List<Question> {
        val rawList = listOf(
            // Harappan Chronology
            createQuestion(
                "IVC-HQ-001", "Harappan Chronology",
                "The mature phase of the Harappan urban civilization is generally dated to",
                "c. 2600–1900 BCE", "c. 3200–2800 BCE", "c. 1500–600 BCE", "c. 600–300 BCE", "A",
                "The commonly used archaeological range for the Mature Harappan phase is about 2600–1900 BCE."
            ),
            createQuestion(
                "IVC-HQ-002", "Harappan Chronology",
                "Which chronological phase of the Indus Valley Civilization is characterized by regional chalcolithic traditions leading to urban integration?",
                "Early Harappan Phase", "Mature Harappan Phase", "Late Harappan Phase", "Post-Harappan Iron Age", "A",
                "The Early Harappan phase (c. 3300–2600 BCE) prepared the foundation for urbanization."
            ),
            createQuestion(
                "IVC-HQ-240", "Harappan Chronology",
                "Radiocarbon dating by D.P. Agrawal revised the Mature Harappan date range to approximately",
                "c. 2300–1750 BCE", "c. 3000–2000 BCE", "c. 2000–1000 BCE", "c. 1500–500 BCE", "A",
                "D.P Agrawal suggested 2300-1750 BCE based on uncalibrated C-14 dates."
            ),
            createQuestion(
                "IVC-HQ-244", "Harappan Chronology",
                "The Early Harappan phase at Kot Diji and Amri is best characterized as",
                "Proto-urban phase with fortification and regional pottery", "Fully urban empire", "Pastoral nomadic stage", "Iron manufacturing hub", "A",
                "Kot Diji and Amri represent proto-urban fortified settlements."
            ),

            // Chronology
            createQuestion(
                "IVC-HQ-241", "Chronology",
                "The Transition from Early to Mature Harappan is best observed archaeologically at",
                "Kalibangan and Kot Diji", "Lothal and Chanhudaro", "Alamgirpur and Manda", "Ropar and Daimabad", "A",
                "Stratigraphic evidence at Kalibangan and Kot Diji shows continuous transition."
            ),
            createQuestion(
                "IVC-HQ-242", "Chronology",
                "The Late Harappan phase is characterized by",
                "De-urbanization, disappearance of script, and localized rural cultures", "Expansion of mega-cities", "Emergence of iron weapons", "Introduction of coinage", "A",
                "Late Harappan phase witnessed de-urbanization and loss of standardized weights/script."
            ),
            createQuestion(
                "IVC-HQ-243", "Chronology",
                "Which site shows an unbroken sequence from Neolithic-Chalcolithic to Mature Harappan?",
                "Mehrgarh", "Lothal", "Surkotada", "Alamgirpur", "A",
                "Mehrgarh in Balochistan demonstrates continuous evolution from 7000 BCE."
            ),

            // Discovery & Naming
            createQuestion(
                "IVC-HQ-003", "Discovery & Naming",
                "The first official announcement of the discovery of the Indus Valley Civilization was made in 1924 by",
                "Sir John Marshall", "Daya Ram Sahni", "R.D. Banerji", "Sir Mortimer Wheeler", "A",
                "Sir John Marshall announced the discovery in the Illustrated London News in September 1924."
            ),
            createQuestion(
                "IVC-HQ-101", "Discovery & Naming",
                "Harappa was first excavated in 1921 under the direction of",
                "Daya Ram Sahni", "R.D. Banerji", "E.J.H. Mackay", "J.P. Joshi", "A",
                "Daya Ram Sahni excavated Harappa in 1921 under John Marshall's supervision."
            ),
            createQuestion(
                "IVC-HQ-102", "Discovery & Naming",
                "Mohenjo-daro was discovered in 1922 by",
                "R.D. Banerji", "Daya Ram Sahni", "Aurel Stein", "N.G. Majumdar", "A",
                "Rakhaldas (R.D.) Banerji discovered Mohenjo-daro in Larkana district, Sindh."
            ),
            createQuestion(
                "IVC-HQ-107", "Discovery & Naming",
                "Who first noticed the ruins of Harappa in 1826?",
                "Charles Masson", "Alexander Burnes", "Alexander Cunningham", "John Fleet", "A",
                "Charles Masson visited Harappa in 1826 and noted its massive brick mounds."
            ),
            createQuestion(
                "IVC-HQ-108", "Discovery & Naming",
                "Alexander Cunningham visited Harappa in 1853 and 1872, but failed to recognize its age because",
                "He tried to fit it into the 6th century BCE Buddhist historical framework", "He thought it was a modern fort", "He found no artifacts", "He believed it was Greek", "A",
                "Cunningham thought the seal belonged to a foreign or Buddhist era."
            ),

            // Discovery
            createQuestion(
                "IVC-HQ-103", "Discovery",
                "The term 'Indus Valley Civilization' was first formally coined and popularized by",
                "Sir John Marshall", "Alexander Cunningham", "D.D. Kosambi", "A.L. Basham", "A",
                "John Marshall formally coined the term 'Indus Valley Civilization'."
            ),
            createQuestion(
                "IVC-HQ-104", "Discovery",
                "Excavations at Chanhudaro were directed by",
                "E.J.H. Mackay and N.G. Majumdar", "R.S. Bisht", "J.P. Joshi", "B.B. Lal", "A",
                "N.G. Majumdar discovered Chanhudaro in 1930 and Mackay excavated in 1935."
            ),
            createQuestion(
                "IVC-HQ-105", "Discovery",
                "Dholavira was discovered in 1967-68 by",
                "J.P. Joshi", "R.S. Bisht", "S.R. Rao", "A. Ghosh", "A",
                "J.P. Joshi of ASI first discovered Dholavira in Kutch."
            ),
            createQuestion(
                "IVC-HQ-106", "Discovery",
                "Extensive excavations at Dholavira from 1990 onwards were conducted by",
                "R.S. Bisht", "B.K. Thapar", "M.K. Dhavalikar", "V.N. Misra", "A",
                "R.S. Bisht led the major ASI excavations at Dholavira."
            ),

            // Geography
            createQuestion(
                "IVC-HQ-004", "Geography",
                "The northernmost site of the Mature Harappan civilization is located at",
                "Manda (Jammu & Kashmir)", "Shortugai", "Ropar", "Alamgirpur", "A",
                "Manda on Chenab river in J&K is the northernmost boundary of the main contiguous zone."
            ),
            createQuestion(
                "IVC-HQ-095", "Geography",
                "The southernmost site of the Harappan civilization is",
                "Daimabad (Maharashtra)", "Lothal", "Rangpur", "Malvan", "A",
                "Daimabad on Pravara river, Maharashtra marks the southern extremity."
            ),
            createQuestion(
                "IVC-HQ-098", "Geography",
                "The easternmost boundary of the Indus Valley Civilization is represented by",
                "Alamgirpur (Uttar Pradesh)", "Hulas", "Bargaon", "Kalibangan", "A",
                "Alamgirpur on Hindon river in UP marks the eastern boundary."
            ),
            createQuestion(
                "IVC-HQ-099", "Geography",
                "The westernmost Harappan trading outpost near the Iranian border is",
                "Sutkagen-dor (Balochistan)", "Sotka-koh", "Balakot", "Dabarkot", "A",
                "Sutkagen-dor on Dasht river near Pak-Iran border is the westernmost limit."
            ),
            createQuestion(
                "IVC-HQ-100", "Geography",
                "Shortugai, an isolated Harappan trading colony established for lapis lazuli sourcing, is located in",
                "Afghanistan", "Tajikistan", "Iran", "Turkmenistan", "A",
                "Shortugai in northern Afghanistan near Oxus river was a trading outpost."
            ),

            // Site Geography
            createQuestion(
                "IVC-HQ-005", "Site Geography",
                "Which Harappan site is located on the banks of the Ravi River?",
                "Harappa", "Mohenjo-daro", "Kalibangan", "Lothal", "A",
                "Harappa in Sahiwal district, Punjab (Pakistan) lies on the Ravi river."
            ),
            createQuestion(
                "IVC-HQ-093", "Site Geography",
                "Mohenjo-daro is situated on the banks of which river?",
                "Indus", "Ravi", "Sutlej", "Ghaggar", "A",
                "Mohenjo-daro is situated on the right bank of the Indus River."
            ),
            createQuestion(
                "IVC-HQ-094", "Site Geography",
                "Lothal is situated near the Gulf of Khambhat on the banks of which river tributary?",
                "Bhogavo (tributary of Sabarmati)", "Narmada", "Tapti", "Mahi", "A",
                "Lothal is situated between the Bhogavo and Sabarmati rivers in Gujarat."
            ),
            createQuestion(
                "IVC-HQ-096", "Site Geography",
                "Kalibangan is situated on the dried bed of which ancient river?",
                "Ghaggar", "Luni", "Chang", "Ravi", "A",
                "Kalibangan in Hanumangarh district, Rajasthan lies on the dried Ghaggar river."
            ),
            createQuestion(
                "IVC-HQ-097", "Site Geography",
                "Rakhigarhi is located in which district of Haryana?",
                "Hisar", "Fatehabad", "Sirsa", "Rohtak", "A",
                "Rakhigarhi is in Hisar district, Haryana."
            ),
            createQuestion(
                "IVC-HQ-262", "Site Geography",
                "Ropar (Rupnagar) is situated on the banks of which river?",
                "Sutlej", "Beas", "Ravi", "Jhelum", "A",
                "Ropar in Punjab lies on the banks of Sutlej river."
            ),

            // Dholavira
            createQuestion(
                "IVC-HQ-006", "Dholavira",
                "A unique feature of Dholavira's urban plan distinguishing it from most Harappan cities is",
                "A three-tiered division into Citadel, Middle Town, and Lower Town", "Complete absence of stone", "Lack of drainage system", "Circular shape layout", "A",
                "Dholavira features a unique tripartite urban division."
            ),
            createQuestion(
                "IVC-HQ-019", "Dholavira",
                "The large wooden inscription board containing ten large Harappan script symbols was discovered at",
                "Dholavira", "Harappa", "Lothal", "Kalibangan", "A",
                "The famous 10-symbol Signboard was found in the citadel gateway at Dholavira."
            ),

            // Lothal
            createQuestion(
                "IVC-HQ-007", "Lothal",
                "The artificial burnt-brick dockyard connected to the tidal waters was discovered at",
                "Lothal", "Surkotada", "Dholavira", "Balakot", "A",
                "Lothal features a world-famous burnt brick dockyard."
            ),

            // Kalibangan
            createQuestion(
                "IVC-HQ-008", "Kalibangan",
                "The earliest evidence of a ploughed field with criss-cross furrows was discovered at",
                "Kalibangan", "Banawali", "Rakhigarhi", "Ropar", "A",
                "Kalibangan yielded a pre-Harappan ploughed field showing double cropping."
            ),

            // Rakhigarhi
            createQuestion(
                "IVC-HQ-009", "Rakhigarhi",
                "Which is currently recognized as the largest Harappan site in India?",
                "Rakhigarhi", "Dholavira", "Kalibangan", "Lothal", "A",
                "Rakhigarhi in Haryana spans over 350 hectares, making it the largest in India."
            ),

            // Major Sites
            createQuestion(
                "IVC-HQ-010", "Major Sites",
                "Chanhudaro is unique among Harappan urban centers because",
                "It lacks a fortified citadel", "It was built entirely of stone", "It had no pottery", "It was ruled by kings", "A",
                "Chanhudaro had no separate citadel mound."
            ),

            // Harappa
            createQuestion(
                "IVC-HQ-031", "Harappa",
                "Coffin burial (Burial R-37) was discovered at which site?",
                "Harappa", "Mohenjo-daro", "Lothal", "Kalibangan", "A",
                "Wood coffin burial R-37 was found at Harappa."
            ),
            createQuestion(
                "IVC-HQ-110", "Harappa",
                "The 'Granary Complex' consisting of 12 granaries arranged in two rows of six was found at",
                "Harappa", "Mohenjo-daro", "Lothal", "Kalibangan", "A",
                "Harappa had 12 granaries on a raised platform near the Ravi river."
            ),
            createQuestion(
                "IVC-HQ-200", "Harappa",
                "Red sandstone male torso showing human anatomy precision was found at",
                "Harappa", "Mohenjo-daro", "Dholavira", "Lothal", "A",
                "The red sandstone male torso was excavated at Harappa."
            ),
            createQuestion(
                "IVC-HQ-261", "Harappa",
                "A bronze chariot model pulled by oxen was excavated at",
                "Daimabad", "Harappa", "Chanhudaro", "Lothal", "A",
                "Daimabad hoard included a famous bronze chariot."
            ),

            // Mohenjo-daro
            createQuestion(
                "IVC-HQ-109", "Mohenjo-daro",
                "The Great Bath and the Great Granary were excavated at",
                "Mohenjo-daro", "Harappa", "Kalibangan", "Banawali", "A",
                "Mohenjo-daro housed the Great Bath and Great Granary."
            ),

            // Great Bath
            createQuestion(
                "IVC-HQ-011", "Great Bath",
                "The floor of the Great Bath at Mohenjo-daro was made watertight using",
                "Burnt bricks set in gypsum mortar backed by bitumen (asphalt)", "Cement plaster", "Granite slabs", "Wood planking", "A",
                "Bitumen (natural asphalt) was used to seal brick joints."
            ),
            createQuestion(
                "IVC-HQ-012", "Great Bath",
                "The dimensions of the Great Bath tank at Mohenjo-daro are approximately",
                "12 m length, 7 m width, 2.4 m depth", "20 m length, 15 m width, 5 m depth", "8 m length, 4 m width, 1 m depth", "30 m length, 10 m width, 3 m depth", "A",
                "The tank measures 11.88 m x 7.01 m x 2.43 m."
            ),
            createQuestion(
                "IVC-HQ-013", "Great Bath",
                "What was the probable primary purpose of the Great Bath?",
                "Ritual purification bathing", "Swimming pool for royalty", "Public drinking water storage", "Grain washing tank", "A",
                "The Great Bath served ceremonial/ritual cleansing purposes."
            ),
            createQuestion(
                "IVC-HQ-111", "Great Bath",
                "Rooms surrounding the Great Bath were likely used for",
                "Changing clothes and priest quarters", "Grain storage", "Weapons locker", "Prison cells", "A",
                "Surrounding rooms provided changing facilities and priest quarters."
            ),

            // Architecture
            createQuestion(
                "IVC-HQ-020", "Architecture",
                "The standard ratio of Harappan burnt brick dimensions (thickness : width : length) was",
                "1 : 2 : 4", "1 : 3 : 6", "2 : 3 : 5", "1 : 1.5 : 3", "A",
                "The brick ratio standard across IVC was strictly 1:2:4."
            ),
            createQuestion(
                "IVC-HQ-125", "Architecture",
                "Most Harappan residential houses opened their doors onto",
                "Side lanes rather than main streets", "Main thoroughfares directly", "Central market square", "River bank directly", "A",
                "Entrances faced quiet side lanes to protect privacy and avoid dust."
            ),
            createQuestion(
                "IVC-HQ-191", "Architecture",
                "Corbelled arches were used by Harappans primarily for",
                "Roofing large culverts and drains", "Grand temple gates", "Palace domes", "City wall bastions", "A",
                "True arches were unknown; corbelled arches supported heavy drain roofs."
            ),

            // Urban Engineering
            createQuestion(
                "IVC-HQ-014", "Urban Engineering",
                "The grid system of town planning in Harappan cities meant that streets crossed each other at",
                "Right angles (90 degrees)", "Acute angles (45 degrees)", "Random curves", "Concentric circles", "A",
                "Streets ran north-south and east-west, intersecting at right angles."
            ),
            createQuestion(
                "IVC-HQ-017", "Urban Engineering",
                "Soak pits and manholes in Harappan drainage systems were placed at regular intervals to",
                "Collect solid waste and allow periodic cleaning", "Store drinking water", "Trap fish", "Divert flood waters", "A",
                "Manholes with removable stone covers allowed cleaning out silt."
            ),
            createQuestion(
                "IVC-HQ-018", "Urban Engineering",
                "House drains emptied wastewater into",
                "Covered street drains running parallel to roads", "Open pits in the courtyard", "River directly", "Underground wells", "A",
                "Private clay pipes connected directly into public covered drains."
            ),

            // Drainage System
            createQuestion(
                "IVC-HQ-015", "Drainage System",
                "Harappan street drains were covered with",
                "Brick slabs or limestone flags", "Wooden logs", "Thatch mats", "Metal grates", "A",
                "Covering drains with stone/brick slabs prevented odor and disease."
            ),
            createQuestion(
                "IVC-HQ-117", "Drainage System",
                "Drains from private bathrooms drained into street sewers through",
                "Terra-cotta pipes built into walls", "Copper tubes", "Bamboo shafts", "Open channels across rooms", "A",
                "Terracotta pipes embedded in walls carried wastewater downward."
            ),
            createQuestion(
                "IVC-HQ-118", "Drainage System",
                "Wooden drain pipes were discovered at",
                "Kalibangan", "Lothal", "Harappa", "Surkotada", "A",
                "Kalibangan yielded unique hollowed-out wooden drain conduits."
            ),

            // Maritime Trade
            createQuestion(
                "IVC-HQ-021", "Maritime Trade",
                "In Cuneiform inscriptions of Mesopotamia, the Harappan region is referred to as",
                "Meluhha", "Dilmun", "Magan", "Elam", "A",
                "Sargon of Akkad texts mention ships from Meluhha (Indus region)."
            ),
            createQuestion(
                "IVC-HQ-022", "Maritime Trade",
                "Cuneiform texts mention two intermediate trading stations between Mesopotamia and Meluhha:",
                "Dilmun (Bahrain) and Magan (Oman)", "Susa and Ur", "Uruk and Kish", "Nippur and Lagash", "A",
                "Dilmun (Bahrain) and Magan (Oman Peninsula) served as maritime trade hubs."
            ),
            createQuestion(
                "IVC-HQ-023", "Maritime Trade",
                "A circular steatite seal of Persian Gulf origin was excavated at",
                "Lothal", "Harappa", "Kalibangan", "Banawali", "A",
                "A round Persian Gulf seal was found at Lothal port."
            ),

            // Trade & Economy
            createQuestion(
                "IVC-HQ-025", "Trade & Economy",
                "The Harappan economy was primarily based on",
                "Agricultural surplus combined with flourishing craft production and trade", "Nomadic cattle rearing", "Mining and war plunder", "Forestry and hunting", "A",
                "Agricultural surplus powered urban specialization and trade networks."
            ),
            createQuestion(
                "IVC-HQ-036", "Trade & Economy",
                "Harappan trade was conducted primarily through",
                "Barter system based on standardized weights", "Gold coin currency", "Silver bullion bars", "Paper receipts", "A",
                "No metallic coins were used; barter was regulated by chert weights."
            ),
            createQuestion(
                "IVC-HQ-059", "Trade & Economy",
                "Harappan weights were generally made of",
                "Chert stone", "Terracotta clay", "Iron ore", "Wood", "A",
                "Chert was the standard material for precision cubical weights."
            ),
            createQuestion(
                "IVC-HQ-073", "Trade & Economy",
                "Ivory scale for linear measurement was found at",
                "Lothal", "Harappa", "Mohenjo-daro", "Kalibangan", "A",
                "An ivory measuring scale was discovered at Lothal."
            ),
            createQuestion(
                "IVC-HQ-091", "Trade & Economy",
                "Chanhudaro was famous as an industrial center for",
                "Bead making and shell cutting", "Shipbuilding", "Stone quarrying", "Textile weaving only", "A",
                "Chanhudaro was an exclusive craft township for beads, seals, and shells."
            ),
            createQuestion(
                "IVC-HQ-151", "Trade & Economy",
                "The Harappan weight ratio followed",
                "Binary system for lower values and decimal for higher values", "Octal system throughout", "Hexadecimal system", "Purely random values", "A",
                "Weights followed binary 1,2,4,8,16,32,64 and decimal 160, 320, 640."
            ),
            createQuestion(
                "IVC-HQ-221", "Trade & Economy",
                "Harappans were the first in the world to cultivate",
                "Cotton", "Tea", "Coffee", "Tobacco", "A",
                "Indus people were pioneers in cotton cultivation and textile weaving."
            ),
            createQuestion(
                "IVC-HQ-222", "Trade & Economy",
                "Greeks called Indian cotton 'Sindon' because",
                "It was derived from Sindhu (Indus)", "It came from Sidon city", "It was silk-like", "It was blue-dyed", "A",
                "Greek 'Sindon' is derived directly from 'Sindhu'."
            ),

            // Art & Crafts
            createQuestion(
                "IVC-HQ-024", "Art & Crafts",
                "The famous bronze statue of the 'Dancing Girl' was created using which technique?",
                "Lost-Wax (Cire Perdue) casting", "Hammered sheet metal", "Stone carving", "Clay moulding", "A",
                "The Dancing Girl statuette was cast via the lost-wax bronze process."
            ),
            createQuestion(
                "IVC-HQ-029", "Art & Crafts",
                "The steatite bust of the 'Priest-King' wears a shawl decorated with",
                "Trefoil (three-leafed) motif", "Swastika pattern", "Lotus flower pattern", "Chequered squares", "A",
                "The Priest-King's shawl features trefoil motifs filled with red pigment."
            ),
            createQuestion(
                "IVC-HQ-062", "Art & Crafts",
                "Harappan pottery was predominantly",
                "Red and Black ware", "Painted Grey Ware", "Northern Black Polished Ware", "Ochre Coloured Pottery", "A",
                "Red pottery painted with black designs was characteristic."
            ),

            // Dancing Girl
            createQuestion(
                "IVC-HQ-043", "Dancing Girl",
                "The Bronze Dancing Girl height is approximately",
                "10.5 cm", "25 cm", "50 cm", "5 cm", "A",
                "The famous bronze figurine stands 10.5 cm tall."
            ),
            createQuestion(
                "IVC-HQ-044", "Dancing Girl",
                "The left arm of the Bronze Dancing Girl is covered with",
                "Bangles made of shell/ivory up to the shoulder", "Tattoos", "Gold armlets", "Iron cuffs", "A",
                "24 to 25 bangles cover her left arm up to the shoulder."
            ),

            // Priest-King Figure
            createQuestion(
                "IVC-HQ-045", "Priest-King Figure",
                "The Priest-King figure height is approximately",
                "17.5 cm", "35 cm", "10 cm", "50 cm", "A",
                "The steatite Priest-King bust measures 17.5 cm in height."
            ),

            // Religion
            createQuestion(
                "IVC-HQ-028", "Religion",
                "The Pasupati Seal depicts a seated figure surrounded by five animals:",
                "Elephant, Tiger, Rhinoceros, Buffalo, and two Deer at feet", "Lion, Horse, Bull, Bear, Dog", "Cow, Goat, Sheep, Peacock, Snake", "Camel, Boar, Panther, Wolf, Monkey", "A",
                "The Pasupati seal depicts Elephant, Tiger, Rhino, Buffalo, and 2 Deer."
            ),
            createQuestion(
                "IVC-HQ-174", "Religion",
                "Sir John Marshall identified the Pasupati figure as",
                "Proto-Shiva", "Indra", "Varuna", "Agni", "A",
                "John Marshall coined the term 'Proto-Shiva' for the Pasupati seal figure."
            ),
            createQuestion(
                "IVC-HQ-176", "Religion",
                "Tree worship was focused mainly on",
                "Pipal tree", "Banyan tree", "Neem tree", "Mango tree", "A",
                "Pipal leaves and tree deities are prominent on Harappan seals."
            ),
            createQuestion(
                "IVC-HQ-290", "Religion",
                "Harappan religion lacked",
                "Grand monumental temples or royal tombs", "Sacred animal reverence", "Water ritual cleansing", "Mother Goddess figures", "A",
                "Unlike Egypt/Mesopotamia, IVC had no monumental temples or royal tombs."
            ),

            // Burials & Funerary Practices
            createQuestion(
                "IVC-HQ-032", "Burials & Funerary Practices",
                "The most common Harappan burial method was",
                "Extended burial with head pointing North", "Cremation with ashes scattered", "Exposure to vultures", "Mummification", "A",
                "Dead bodies were laid extended in North-South orientation."
            ),

            // Animals & Domestication
            createQuestion(
                "IVC-HQ-086", "Animals & Domestication",
                "Which animal was NOT depicted on Harappan seals?",
                "Horse", "Elephant", "Tiger", "Bull", "A",
                "Horse is conspicuously absent from standard Harappan seals."
            ),
            createQuestion(
                "IVC-HQ-089", "Animals & Domestication",
                "The most frequently depicted animal on Harappan seals is",
                "Unicorn (mythical single-horned bull)", "Humped Bull", "Elephant", "Tiger", "A",
                "Unicorn motif accounts for over 60% of animal seals."
            ),

            // Water Management
            createQuestion(
                "IVC-HQ-033", "Water Management",
                "Dholavira had a sophisticated water harvesting system comprising",
                "16 large reservoirs cut into rock around the city", "Single deep lake", "Canals from Ganga", "Desalination plants", "A",
                "Dholavira engineered 16 mega reservoirs around the city walls."
            ),

            // Metallurgy
            createQuestion(
                "IVC-HQ-041", "Metallurgy",
                "Which metal was completely UNKNOWN to the Harappans?",
                "Iron", "Copper", "Gold", "Silver", "A",
                "Iron was unknown; IVC belonged to the Bronze/Chalcolithic Age."
            ),

            // Harappan Seals
            createQuestion(
                "IVC-HQ-048", "Harappan Seals",
                "Most Harappan seals were made of",
                "Steatite (Soapstone)", "Terracotta", "Copper", "Gold", "A",
                "Soft steatite stone was carved and fired to produce durable seals."
            ),

            // Indus Script
            createQuestion(
                "IVC-HQ-051", "Indus Script & Inscriptions",
                "The direction of writing in the Indus script was",
                "Right to left in first line, left to right in second line (Boustrophedon)", "Left to right only", "Top to bottom only", "Bottom to top", "A",
                "Indus script used Boustrophedon writing direction."
            ),
            createQuestion(
                "IVC-HQ-053", "Indus Script & Inscriptions",
                "The longest Indus script inscription containing 26 signs was found at",
                "Dholavira signboard", "Harappa seal", "Mohenjo-daro brick", "Lothal jar", "A",
                "The 10-symbol signboard at Dholavira and 26-sign inscription are the longest."
            ),

            // Heritage & Legacy
            createQuestion(
                "IVC-HQ-207", "Heritage",
                "Which Harappan element continues in modern Indian culture?",
                "Pipal tree worship, Namaste gesture, Swastika, and Vermilion (Sindoor)", "Pyramid tombs", "Iron weaponry", "Cuneiform writing", "A",
                "Pipal worship, Swastika, Sindoor, and Namaste are enduring IVC legacies."
            ),
            createQuestion(
                "IVC-HQ-208", "Heritage",
                "Binary weight ratio 16 survived in traditional Indian coinage as",
                "16 Annas = 1 Rupee", "10 Paise = 1 Rupee", "100 Kori = 1 Rupee", "12 Dinar = 1 Rupee", "A",
                "The 16 Annas = 1 Rupee system directly inherited the Harappan 16 ratio."
            ),

            // Decline
            createQuestion(
                "IVC-HQ-229", "Decline",
                "The 'Aryan Invasion Theory' for Harappan decline was proposed by",
                "Sir Mortimer Wheeler", "John Marshall", "D.P. Agrawal", "H.T. Lambrick", "A",
                "Mortimer Wheeler cited Rigvedic Indra as 'Purandara' for the invasion hypothesis."
            ),
            createQuestion(
                "IVC-HQ-231", "Decline",
                "Ecological degradation and drying up of Sarasvati River theory was proposed by",
                "D.P. Agrawal and A. Ghosh", "Mortimer Wheeler", "R.D. Banerji", "Charles Masson", "A",
                "Agrawal and Ghosh attributed decline to drying of Ghaggar-Hakra (Sarasvati)."
            ),

            // Synthesis
            createQuestion(
                "IVC-HQ-300", "Synthesis",
                "The legacy of Indus Valley Civilization is best described as",
                "A foundational bedrock of Indian cultural, architectural, and civic traditions", "A short-lived foreign colony", "An isolated nomadic phase", "A forgotten stone-age myth", "A",
                "IVC forms the foundational urban bedrock of South Asian history."
            )
        )

        // Assign Set Names (10 Qs per Set)
        var currentSetNum = 1
        return rawList.chunked(10).flatMap { chunk ->
            val sName = "Set $currentSetNum"
            currentSetNum++
            chunk.map { q -> q.copy(setName = sName) }
        }
    }
}
