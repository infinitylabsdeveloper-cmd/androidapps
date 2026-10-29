package com.example.data

import com.example.model.CurrentAffair
import com.example.model.MonthlyCurrentAffairsModel
import com.example.model.MonthlyMcqSetModel
import com.example.model.Question

object CurrentAffairsData {

    val august2025Questions: List<Question> = listOf(
        Question(
            id = "ca-aug-2025-q01",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Monetary Policy & Economy",
            questionText = "What was the repo rate retained by the RBI Monetary Policy Committee in August 2025?",
            options = mapOf(
                "A" to "5.00%",
                "B" to "5.25%",
                "C" to "5.50%",
                "D" to "5.75%"
            ),
            correctOption = "C",
            explanation = "The RBI Monetary Policy Committee kept the repo rate unchanged at 5.50% in August 2025.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q02",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "National Economy & GDP",
            questionText = "What was India's real GDP growth rate in Q1 of FY 2025-26, according to the August 2025 data?",
            options = mapOf(
                "A" to "6.5%",
                "B" to "7.0%",
                "C" to "7.8%",
                "D" to "8.4%"
            ),
            correctOption = "C",
            explanation = "India's real GDP grew by 7.8% in the first quarter of FY 2025-26.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q03",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Science, Tech & Space",
            questionText = "On which date was National Space Day celebrated in India in 2025?",
            options = mapOf(
                "A" to "15 August",
                "B" to "23 August",
                "C" to "24 August",
                "D" to "29 August"
            ),
            correctOption = "B",
            explanation = "National Space Day was celebrated on 23 August 2025. The 2025 theme was 'Aryabhatta to Gaganyaan: Ancient Wisdom to Infinite Possibilities'.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q04",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Science, Tech & Space",
            questionText = "What was the theme of National Space Day 2025?",
            options = mapOf(
                "A" to "Space for Sustainable Development",
                "B" to "Aryabhatta to Gaganyaan: Ancient Wisdom to Infinite Possibilities",
                "C" to "India in Space: A New Era",
                "D" to "Gaganyaan: Journey to the Stars"
            ),
            correctOption = "B",
            explanation = "ISRO stated that the theme of National Space Day 2025 was 'Aryabhatta to Gaganyaan: Ancient Wisdom to Infinite Possibilities'.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q05",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "ISRO & Gaganyaan Mission",
            questionText = "Which mission was associated with ISRO's first Integrated Air Drop Test conducted in August 2025?",
            options = mapOf(
                "A" to "Chandrayaan-4",
                "B" to "Aditya-L2",
                "C" to "Gaganyaan",
                "D" to "Mangalyaan-2"
            ),
            correctOption = "C",
            explanation = "ISRO conducted its first Integrated Air Drop Test to demonstrate the parachute-based deceleration system for the Gaganyaan human spaceflight mission.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q06",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Defence & Aerospace Operations",
            questionText = "Which organizations participated in the Integrated Air Drop Test for Gaganyaan in August 2025?",
            options = mapOf(
                "A" to "ISRO, DRDO, Indian Air Force, Indian Navy and Indian Coast Guard",
                "B" to "ISRO and NASA only",
                "C" to "DRDO and Indian Army only",
                "D" to "ISRO and European Space Agency only"
            ),
            correctOption = "A",
            explanation = "The IADT-01 was a joint effort involving ISRO, the Indian Air Force, DRDO, the Indian Navy and the Indian Coast Guard.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q07",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Important Days & Personalities",
            questionText = "National Sports Day is observed in India on which date?",
            options = mapOf(
                "A" to "15 August",
                "B" to "23 August",
                "C" to "29 August",
                "D" to "31 August"
            ),
            correctOption = "C",
            explanation = "National Sports Day is observed on 29 August to commemorate the birth anniversary of hockey legend Major Dhyan Chand.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q08",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Sports & Fit India",
            questionText = "What was the theme of the three-day National Sports Day 2025 movement?",
            options = mapOf(
                "A" to "Khelo India",
                "B" to "Ek Ghanta, Khel ke Maidan Main",
                "C" to "Fit India, Strong India",
                "D" to "Har Ghar Khel"
            ),
            correctOption = "B",
            explanation = "The 2025 National Sports Day celebrations from 29–31 August were organized under the theme 'Ek Ghanta, Khel ke Maidan Main'.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q09",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Sports & Legends",
            questionText = "Who was the National Sports Day 2025 movement designed to commemorate?",
            options = mapOf(
                "A" to "Milkha Singh",
                "B" to "Major Dhyan Chand",
                "C" to "Dhanraj Pillay",
                "D" to "Balbir Singh Sr."
            ),
            correctOption = "B",
            explanation = "National Sports Day commemorates the birth anniversary of Major Dhyan Chand, the legendary Indian hockey player.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q10",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Government Schemes & Outlay",
            questionText = "What was the outlay announced for the Pradhan Mantri Viksit Bharat Rozgar Yojana in August 2025?",
            options = mapOf(
                "A" to "₹50,000 crore",
                "B" to "₹75,000 crore",
                "C" to "₹1 lakh crore",
                "D" to "₹2 lakh crore"
            ),
            correctOption = "C",
            explanation = "The Pradhan Mantri Viksit Bharat Rozgar Yojana was announced with an outlay of ₹1 lakh crore.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q11",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Employment & Development Schemes",
            questionText = "The Pradhan Mantri Viksit Bharat Rozgar Yojana aimed to support the creation of how many jobs over two years?",
            options = mapOf(
                "A" to "1 crore",
                "B" to "2 crore",
                "C" to "3.5 crore",
                "D" to "5 crore"
            ),
            correctOption = "C",
            explanation = "The scheme was announced with the aim of supporting the creation of more than 3.5 crore jobs over two years.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q12",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Youth Welfare & Incentives",
            questionText = "What was the maximum financial incentive for newly employed youth under the Pradhan Mantri Viksit Bharat Rozgar Yojana?",
            options = mapOf(
                "A" to "₹5,000",
                "B" to "₹10,000",
                "C" to "₹15,000",
                "D" to "₹25,000"
            ),
            correctOption = "C",
            explanation = "The scheme provides financial incentives of up to ₹15,000 to newly employed youth, disbursed in two instalments.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q13",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Employer Subsidies & Schemes",
            questionText = "How much monthly support could employers receive per new employee under the Pradhan Mantri Viksit Bharat Rozgar Yojana?",
            options = mapOf(
                "A" to "₹1,000",
                "B" to "₹2,000",
                "C" to "₹3,000",
                "D" to "₹5,000"
            ),
            correctOption = "C",
            explanation = "Employers could receive support of up to ₹3,000 per month for each new employee under the scheme.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q14",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Indian Polity & Parliament",
            questionText = "How many sittings were held during the Monsoon Session of Parliament that concluded in August 2025?",
            options = mapOf(
                "A" to "18",
                "B" to "21",
                "C" to "24",
                "D" to "28"
            ),
            correctOption = "B",
            explanation = "The Monsoon Session of Parliament concluded on 21 August 2025 after 21 sittings.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q15",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Legislative Affairs & Bills",
            questionText = "How many Bills were introduced during the Monsoon Session of Parliament in 2025?",
            options = mapOf(
                "A" to "9",
                "B" to "11",
                "C" to "13",
                "D" to "16"
            ),
            correctOption = "C",
            explanation = "According to PRS Legislative Research, 13 Bills were introduced during the 2025 Monsoon Session.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q16",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Parliamentary Enactments",
            questionText = "How many Bills were passed during the 2025 Monsoon Session of Parliament?",
            options = mapOf(
                "A" to "10",
                "B" to "12",
                "C" to "14",
                "D" to "17"
            ),
            correctOption = "C",
            explanation = "PRS reported that 14 Bills were passed during the Monsoon Session of Parliament in 2025.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q17",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Tax Reforms & Legislation",
            questionText = "Which Act was proposed to be replaced by the Income-Tax (No. 2) Bill, 2025?",
            options = mapOf(
                "A" to "Income Tax Act, 1961",
                "B" to "Finance Act, 1991",
                "C" to "Companies Act, 2013",
                "D" to "GST Act, 2017"
            ),
            correctOption = "A",
            explanation = "The Income-Tax (No. 2) Bill, 2025 was intended to replace the Income-Tax Act, 1961, while retaining most of its provisions in simplified language.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q18",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Economic Indices & Inflation",
            questionText = "What was the provisional CPI-based headline inflation rate for August 2025?",
            options = mapOf(
                "A" to "1.61%",
                "B" to "2.07%",
                "C" to "2.50%",
                "D" to "3.14%"
            ),
            correctOption = "B",
            explanation = "The provisional All-India CPI headline inflation rate for August 2025 was 2.07% year-on-year.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q19",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Food Inflation & Price Index",
            questionText = "What was India's food inflation rate based on the Consumer Food Price Index in August 2025?",
            options = mapOf(
                "A" to "-0.69%",
                "B" to "0.69%",
                "C" to "1.76%",
                "D" to "2.07%"
            ),
            correctOption = "A",
            explanation = "The provisional All-India Consumer Food Price Index inflation rate for August 2025 was -0.69% year-on-year.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q20",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Industrial Production & Growth",
            questionText = "What year-on-year growth did India's Index of Industrial Production record in August 2025?",
            options = mapOf(
                "A" to "3.0%",
                "B" to "3.5%",
                "C" to "4.0%",
                "D" to "4.8%"
            ),
            correctOption = "C",
            explanation = "The quick estimate showed IIP growth of 4.0% in August 2025.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q21",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "IIP Sectors & Mining",
            questionText = "Which IIP sector recorded 6.0% growth in August 2025?",
            options = mapOf(
                "A" to "Manufacturing",
                "B" to "Mining",
                "C" to "Electricity",
                "D" to "Construction"
            ),
            correctOption = "B",
            explanation = "The mining sector recorded 6.0% growth in August 2025, while manufacturing grew 3.8% and electricity 4.1%.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q22",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "National Awards & Education",
            questionText = "Which ministry's higher education department selected 21 teachers for the National Awards to Teachers 2025?",
            options = mapOf(
                "A" to "Ministry of Education",
                "B" to "Ministry of Science and Technology",
                "C" to "Ministry of Skill Development",
                "D" to "Ministry of Culture"
            ),
            correctOption = "A",
            explanation = "The Department of Higher Education under the Ministry of Education selected 21 teachers from higher education institutions and polytechnics for the National Awards to Teachers 2025.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q23",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Andhra Pradesh State Affairs",
            questionText = "Which Andhra Pradesh institution had a faculty member among the National Awards to Teachers 2025 selections?",
            options = mapOf(
                "A" to "Andhra University",
                "B" to "School of Planning and Architecture, Vijayawada",
                "C" to "IIT Tirupati",
                "D" to "Acharya Nagarjuna University"
            ),
            correctOption = "B",
            explanation = "Prof. Vijayalaxmi J from the School of Planning and Architecture, Vijayawada, Andhra Pradesh, was listed among the 2025 selections.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q24",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Governance & Anubhav Portal",
            questionText = "How many write-ups had been published on the Anubhav portal over its first 10 years, according to the August 2025 PIB release?",
            options = mapOf(
                "A" to "More than 5,000",
                "B" to "More than 8,000",
                "C" to "More than 12,500",
                "D" to "More than 20,000"
            ),
            correctOption = "C",
            explanation = "The PIB release stated that the Anubhav portal had published more than 12,500 write-ups over its 10-year period.",
            language = "English"
        ),
        Question(
            id = "ca-aug-2025-q25",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "National Anubhav Awards",
            questionText = "How many awardees were selected for the National Anubhav Awards 2025?",
            options = mapOf(
                "A" to "10",
                "B" to "15",
                "C" to "20",
                "D" to "25"
            ),
            correctOption = "B",
            explanation = "Fifteen awardees from 11 Ministries or Departments were selected for the National Anubhav Awards 2025.",
            language = "English"
        )
    )

    val july2025Questions: List<Question> = listOf(
        Question(
            id = "ca-jul-2025-q01",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Economic Surveys & Union Budget",
            questionText = "Which key focus area was designated under Pillar 1 of the Union Budget 2025-26 presented in July 2025?",
            options = mapOf(
                "A" to "Productivity and Resilience in Agriculture",
                "B" to "Deep Ocean Mining",
                "C" to "Defense Modernization",
                "D" to "High-Speed Rail Networks"
            ),
            correctOption = "A",
            explanation = "Pillar 1 of the Union Budget 2025-26 prioritized Productivity and Resilience in Agriculture to boost rural farm incomes.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q02",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Andhra Pradesh State Projects",
            questionText = "What financial assistance was announced by the Central Government for the capital city development of Amaravati in July 2025?",
            options = mapOf(
                "A" to "Rs. 5,000 Crore",
                "B" to "Rs. 10,000 Crore",
                "C" to "Rs. 15,000 Crore through multilateral agencies",
                "D" to "Rs. 25,000 Crore direct grant"
            ),
            correctOption = "C",
            explanation = "The Central Government facilitated special financial support of Rs. 15,000 crore through multilateral development agencies for the development of Amaravati capital.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q03",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Science & Defence Tech",
            questionText = "Which indigenous air defence missile system completed its final user evaluation trials by DRDO in July 2025?",
            options = mapOf(
                "A" to "Akash Prime",
                "B" to "VL-SRSAM",
                "C" to "QRSAM",
                "D" to "Prithvi Air Defence"
            ),
            correctOption = "C",
            explanation = "The Quick Reaction Surface-to-Air Missile (QRSAM) system successfully completed user trials by DRDO and the Indian Army.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q04",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "International Relations & Summits",
            questionText = "Where was the 2025 SCO (Shanghai Cooperation Organisation) Heads of State Summit held?",
            options = mapOf(
                "A" to "Astana, Kazakhstan",
                "B" to "Tashkent, Uzbekistan",
                "C" to "Bishkek, Kyrgyzstan",
                "D" to "New Delhi, India"
            ),
            correctOption = "A",
            explanation = "The SCO Summit 2025 took place in Astana, Kazakhstan, discussing regional security and trade.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q05",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "AP Polavaram Project",
            questionText = "Which milestone was achieved in the Polavaram Irrigation Project in July 2025?",
            options = mapOf(
                "A" to "Completion of Left Main Canal lining",
                "B" to "Approval of Phase-1 Revised Cost Estimates and diaphragm wall reconstruction",
                "C" to "Full reservoir commissioning",
                "D" to "Hydropower generation started"
            ),
            correctOption = "B",
            explanation = "The Union Cabinet sanctioned the expedited funding for the construction of a new diaphragm wall and Phase-1 completion of the Polavaram project.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q06",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Awards & Honors",
            questionText = "Who was awarded the prestigious Lokmanya Tilak National Award in 2025?",
            options = mapOf(
                "A" to "Dr. S. Somanath",
                "B" to "Sudha Murty",
                "C" to "Ratan Tata",
                "D" to "Justice D.Y. Chandrachud"
            ),
            correctOption = "A",
            explanation = "ISRO Chairman Dr. S. Somanath was honored for his exemplary leadership in India's space achievements.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q07",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Renewable Energy & Environment",
            questionText = "India achieved what percentage of its total installed electricity capacity from non-fossil fuel sources by July 2025?",
            options = mapOf(
                "A" to "38.5%",
                "B" to "42.0%",
                "C" to "46.2%",
                "D" to "50.0%"
            ),
            correctOption = "C",
            explanation = "India crossed 46.2% non-fossil capacity, advancing toward its 500 GW target by 2030.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q08",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Sports & Athletics",
            questionText = "Which country hosted the World Athletics Relays in 2025?",
            options = mapOf(
                "A" to "Nassau, Bahamas",
                "B" to "Guangzhou, China",
                "C" to "Doha, Qatar",
                "D" to "Paris, France"
            ),
            correctOption = "B",
            explanation = "Guangzhou, China hosted the World Athletics Relays 2025.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q09",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Digital Public Infrastructure",
            questionText = "Which portal was launched by the Ministry of Electronics and IT for unified cyber fraud reporting in 2025?",
            options = mapOf(
                "A" to "Chakshu Portal",
                "B" to "Cyber Suraksha Kendra",
                "C" to "Digital Kavach 2.0",
                "D" to "I4C Unified Shield"
            ),
            correctOption = "A",
            explanation = "The Chakshu facility under Sanchar Saathi was expanded to combat suspect fraudulent communications.",
            language = "English"
        ),
        Question(
            id = "ca-jul-2025-q10",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "AP Industrial Policy",
            questionText = "Which industrial corridor node in Andhra Pradesh received greenfield clearance in the 2025 union announcements?",
            options = mapOf(
                "A" to "Kopparthy in Kadapa",
                "B" to "Orvakal in Kurnool",
                "C" to "Both Kopparthy and Orvakal",
                "D" to "Nakkapalli only"
            ),
            correctOption = "C",
            explanation = "Under the Vizag-Chennai and Hyderabad-Bengaluru industrial corridors, nodes at Kopparthy and Orvakal were fast-tracked.",
            language = "English"
        )
    )

    val june2025Questions: List<Question> = listOf(
        Question(
            id = "ca-jun-2025-q01",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "General Elections & Polity",
            questionText = "Who was elected as the Speaker of the 18th Lok Sabha in June 2024/2025?",
            options = mapOf(
                "A" to "Om Birla",
                "B" to "K. Suresh",
                "C" to "Bhartruhari Mahtab",
                "D" to "Rajnath Singh"
            ),
            correctOption = "A",
            explanation = "Shri Om Birla was re-elected as the Speaker of the 18th Lok Sabha.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q02",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Environment & World Environment Day",
            questionText = "What was the official global theme for World Environment Day on 5th June 2025?",
            options = mapOf(
                "A" to "Land restoration, desertification and drought resilience",
                "B" to "Beat Plastic Pollution",
                "C" to "Only One Earth",
                "D" to "Ecosystem Restoration"
            ),
            correctOption = "A",
            explanation = "The focus of World Environment Day was 'Land restoration, desertification and drought resilience' under the slogan 'Our land. Our future.'",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q03",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "AP State Governance",
            questionText = "Who took oath as the Chief Minister of Andhra Pradesh following the 2024 assembly elections?",
            options = mapOf(
                "A" to "N. Chandrababu Naidu",
                "B" to "Y.S. Jagan Mohan Reddy",
                "C" to "Pawan Kalyan",
                "D" to "Nara Lokesh"
            ),
            correctOption = "A",
            explanation = "N. Chandrababu Naidu was sworn in as the 18th Chief Minister of Andhra Pradesh at Kesarapalli near Vijayawada.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q04",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "International Summits",
            questionText = "Where was the 50th G7 Summit held in June 2024/2025?",
            options = mapOf(
                "A" to "Apulia, Italy",
                "B" to "Hiroshima, Japan",
                "C" to "Biarritz, France",
                "D" to "Kananaskis, Canada"
            ),
            correctOption = "A",
            explanation = "The 50th G7 summit was held in Borgo Egnazia, Apulia, Italy, where Prime Minister Narendra Modi was invited as an outreach guest.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q05",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Science & Space Exploration",
            questionText = "Which country successfully returned samples from the far side of the Moon for the first time in history in June?",
            options = mapOf(
                "A" to "China (Chang'e-6)",
                "B" to "USA (Artemis-2)",
                "C" to "India (Chandrayaan-3)",
                "D" to "Japan (SLIM)"
            ),
            correctOption = "A",
            explanation = "China's Chang'e-6 lunar probe successfully collected and returned the first-ever soil and rock samples from the Moon's far side.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q06",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "International Yoga Day",
            questionText = "What was the theme for the 10th International Day of Yoga celebrated on June 21?",
            options = mapOf(
                "A" to "Yoga for Self and Society",
                "B" to "Yoga for Vasudhaiva Kutumbakam",
                "C" to "Yoga for Humanity",
                "D" to "Yoga for Well-being"
            ),
            correctOption = "A",
            explanation = "The theme for International Yoga Day was 'Yoga for Self and Society'.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q07",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Sports & ICC Cricket",
            questionText = "Which team won the ICC Men's T20 World Cup in June?",
            options = mapOf(
                "A" to "India",
                "B" to "South Africa",
                "C" to "Australia",
                "D" to "England"
            ),
            correctOption = "A",
            explanation = "India defeated South Africa by 7 runs in Barbados to win the ICC Men's T20 World Cup title.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q08",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Indian Criminal Law Reforms",
            questionText = "On which date did the three new criminal laws (BNS, BNSS, and BSA) come into nationwide effect?",
            options = mapOf(
                "A" to "1st July 2024",
                "B" to "15th August 2024",
                "C" to "26th January 2025",
                "D" to "1st May 2025"
            ),
            correctOption = "A",
            explanation = "The Bharatiya Nyaya Sanhita (BNS), Bharatiya Nagarik Suraksha Sanhita (BNSS), and Bharatiya Sakshya Adhiniyam (BSA) came into force from July 1, 2024, replacing IPC, CrPC, and Evidence Act.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q09",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "AP Cabinet Decisions",
            questionText = "Which signature scheme was revived to provide financial support to female heads of household in AP?",
            options = mapOf(
                "A" to "Aadabidda Nidhi / Thalliki Vandanam",
                "B" to "YSR Cheyutha",
                "C" to "Maa Inti Velugu",
                "D" to "Stree Shakti Card"
            ),
            correctOption = "A",
            explanation = "Under the Super Six guarantees, the AP Government announced the implementation of Thalliki Vandanam and Aadabidda Nidhi.",
            language = "English"
        ),
        Question(
            id = "ca-jun-2025-q10",
            appId = "appsc-group2",
            subjectId = "subj-current-affairs",
            contentLevel = "SUBJECT",
            subtopic = "Economics & Forex Reserves",
            questionText = "What historic milestone did India's foreign exchange reserves cross in June?",
            options = mapOf(
                "A" to "$650 Billion",
                "B" to "$700 Billion",
                "C" to "$600 Billion",
                "D" to "$750 Billion"
            ),
            correctOption = "A",
            explanation = "India's foreign exchange reserves crossed an all-time high of $650 billion in June.",
            language = "English"
        )
    )

    val initialMonthlyCurrentAffairs: List<MonthlyCurrentAffairsModel> = listOf(
        MonthlyCurrentAffairsModel(
            id = "ca-August-2025",
            appId = "appsc-group2",
            mappedAppIds = listOf("appsc-group2", "appsc-group1", "ap-dsc", "ap-tet", "ALL"),
            month = "August",
            year = "2025",
            title = "August 2025 Current Affairs",
            sets = listOf(
                MonthlyMcqSetModel(
                    id = "ca-set-August-2025-1",
                    title = "Set 1 (Q1 – Q10)",
                    month = "August",
                    year = "2025",
                    questions = august2025Questions.subList(0, 10)
                ),
                MonthlyMcqSetModel(
                    id = "ca-set-August-2025-2",
                    title = "Set 2 (Q11 – Q20)",
                    month = "August",
                    year = "2025",
                    questions = august2025Questions.subList(10, 20)
                ),
                MonthlyMcqSetModel(
                    id = "ca-set-August-2025-3",
                    title = "Set 3 (Q21 – Q25)",
                    month = "August",
                    year = "2025",
                    questions = august2025Questions.subList(20, 25)
                )
            ),
            isActive = true,
            startDate = "2024-01-01",
            startTime = "00:00",
            endDate = "2030-12-31",
            endTime = "23:59"
        ),
        MonthlyCurrentAffairsModel(
            id = "ca-July-2025",
            appId = "appsc-group2",
            mappedAppIds = listOf("appsc-group2", "appsc-group1", "ap-dsc", "ap-tet", "ALL"),
            month = "July",
            year = "2025",
            title = "July 2025 Current Affairs",
            sets = listOf(
                MonthlyMcqSetModel(
                    id = "ca-set-July-2025-1",
                    title = "Set 1 (Q1 – Q10)",
                    month = "July",
                    year = "2025",
                    questions = july2025Questions
                )
            ),
            isActive = true,
            startDate = "2024-01-01",
            startTime = "00:00",
            endDate = "2030-12-31",
            endTime = "23:59"
        ),
        MonthlyCurrentAffairsModel(
            id = "ca-June-2025",
            appId = "appsc-group2",
            mappedAppIds = listOf("appsc-group2", "appsc-group1", "ap-dsc", "ap-tet", "ALL"),
            month = "June",
            year = "2025",
            title = "June 2025 Current Affairs",
            sets = listOf(
                MonthlyMcqSetModel(
                    id = "ca-set-June-2025-1",
                    title = "Set 1 (Q1 – Q10)",
                    month = "June",
                    year = "2025",
                    questions = june2025Questions
                )
            ),
            isActive = true,
            startDate = "2024-01-01",
            startTime = "00:00",
            endDate = "2030-12-31",
            endTime = "23:59"
        )
    )

    val initialCurrentAffairsArticles: List<CurrentAffair> = august2025Questions.mapIndexed { idx, q ->
        CurrentAffair(
            id = "article-ca-aug25-${idx + 1}",
            appId = "appsc-group2",
            mappedAppIds = listOf("appsc-group2", "appsc-group1", "ap-dsc", "ap-tet", "ALL"),
            month = "August 2025",
            category = when {
                q.subtopic.contains("Andhra", ignoreCase = true) -> "AP Current Affairs"
                q.subtopic.contains("Economy", ignoreCase = true) || q.subtopic.contains("Inflation", ignoreCase = true) -> "Economy"
                q.subtopic.contains("Space", ignoreCase = true) || q.subtopic.contains("Tech", ignoreCase = true) -> "Science & Technology"
                q.subtopic.contains("Polity", ignoreCase = true) || q.subtopic.contains("Parliament", ignoreCase = true) -> "Polity"
                q.subtopic.contains("Sports", ignoreCase = true) -> "Sports"
                q.subtopic.contains("Scheme", ignoreCase = true) -> "Schemes"
                else -> "National"
            },
            title = q.questionText,
            content = q.explanation ?: "August 2025 Current Affairs for APPSC Group-2 exam preparation.",
            publishedDate = "2025-08-31",
            status = "PUBLISHED",
            isSharedMultiApp = true,
            attachedMcqs = listOf(q)
        )
    }
}
