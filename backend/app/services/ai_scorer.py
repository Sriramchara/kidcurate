"""
AI Video Scoring Service
Uses Gemini to analyze YouTube videos for educational quality and child safety.
"""
import json
import re
from typing import Optional, Dict, Any
from app.config import settings


async def score_video(
    youtube_id: str,
    title: str,
    description: str,
    channel_name: str,
    duration_seconds: int,
    target_age: Optional[int] = None,
) -> Optional[Dict[str, Any]]:
    """
    Score a YouTube video using Gemini AI.
    Returns a dict with educational scores and metadata.
    Returns None if scoring fails.
    """
    if not settings.GEMINI_API_KEY:
        return None

    # Try to get transcript for richer analysis
    transcript_text = await _get_transcript(youtube_id)

    age_context = f"Target child age: {target_age}" if target_age else "General children content"
    content_to_analyze = f"""
Title: {title}
Channel: {channel_name}
Duration: {duration_seconds // 60} minutes {duration_seconds % 60} seconds
{age_context}

Description:
{description[:1000]}

Transcript excerpt (first 1000 chars):
{transcript_text[:1000] if transcript_text else "Not available"}
"""

    prompt = f"""You are an expert child safety and moral content evaluator for KidCurate.
Your mission is to strictly protect children from vulgarity, bad language, adult innuendo, screamers, violence, and toxic brainrot tropes (even those frequently found in modern cartoons and YouTube Kids), and to champion pure, moral, heartwarming, and gentle content for kids.

Analyze this video and return a JSON object with your assessment:

{content_to_analyze}

Return ONLY valid JSON with these exact fields:
{{
  "educational_score": <integer 0-100, moral and character value (kindness, honesty, creativity, good habits)>,
  "safety_score": <integer 0-100, vulgarity-free purity score (100 = completely pure & vulgarity-free)>,
  "clarity_score": <integer 0-100, how gentle, engaging, and age-appropriate is this content?>,
  "overall_score": <integer 0-100, composite score weighted towards safety and positive morals>,
  "age_min": <minimum recommended age integer>,
  "age_max": <maximum recommended age integer>,
  "subject": <primary category string, e.g. "Moral Stories", "Animal Friends", "Arts & Crafts", "Innocent Cartoons", "Bedtime Calm">,
  "subject_tags": <array of 3-5 topic strings like "Honesty", "Kindness", "Drawing", "Bedtime", "Zero Vulgarity">,
  "summary": <2-3 sentence wholesome summary describing the moral values, innocence, or creative fun for parents>,
  "is_safe": <boolean, true ONLY if 100% free from vulgarity, violence, or inappropriate themes>,
  "red_flags": <array of concern strings, empty if none>,
  "recommended": <boolean, would you recommend this to parents looking for clean, wholesome content?>
}}

Scoring guide:
- safety_score: 100 = completely pure, gentle, and child-safe. Deduct heavily for: vulgarity, profanity, adult humor, scary content, screaming brainrot tropes, violence, or disrespectful behavior.
- educational_score: 90+ = rich moral values (honesty, kindness, empathy, sharing) or creative skill building (drawing, crafts, singing); 70-89 = innocent gentle fun; <50 = poor or mindless.
- overall_score = (safety_score * 0.6) + (educational_score * 0.4)

Red flags to check: any vulgar slang, suggestive humor, screaming/sensory overload, clickbait, violence, disrespectful behavior portrayed as cool, ads/sponsorships.
"""

    try:
        import warnings
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            import google.generativeai as genai
        genai.configure(api_key=settings.GEMINI_API_KEY)
        model = genai.GenerativeModel("gemini-1.5-flash")
        response = model.generate_content(prompt)
        text = response.text.strip()

        # Extract JSON from response
        json_match = re.search(r'\{.*\}', text, re.DOTALL)
        if json_match:
            return json.loads(json_match.group())
    except Exception as e:
        print(f"AI scoring failed for {youtube_id}: {e}")

    return None


async def _get_transcript(youtube_id: str) -> str:
    """Attempt to fetch YouTube transcript safely without blocking async event loop."""
    def _fetch():
        try:
            from youtube_transcript_api import YouTubeTranscriptApi
            transcript_list = YouTubeTranscriptApi.get_transcript(youtube_id, languages=['en', 'en-US', 'en-GB'])
            return " ".join([entry["text"] for entry in transcript_list])[:4000]
        except Exception:
            try:
                from youtube_transcript_api import YouTubeTranscriptApi
                transcript_list = YouTubeTranscriptApi.get_transcript(youtube_id)
                return " ".join([entry["text"] for entry in transcript_list])[:4000]
            except Exception:
                return ""

    import asyncio
    return await asyncio.to_thread(_fetch)





def _fallback_quiz(title: str) -> Dict[str, Any]:
    return {
        "questions": [
            {
                "question": f"What was the main topic of '{title}'?",
                "options": ["Exploring and learning about the topic", "Cooking a pizza", "Going to sleep", "Driving a racing car"],
                "correct_answer": 0,
                "explanation": "Great job! The video focuses on exploring and teaching this topic."
            },
            {
                "question": "What is the best way to remember what you learned in this video?",
                "options": ["Practice and explain it to a friend", "Forget about it immediately", "Close your eyes and run away", "None of these"],
                "correct_answer": 0,
                "explanation": "Practicing and sharing what you learn helps you remember it best!"
            },
            {
                "question": "Did you discover something exciting from watching?",
                "options": ["Yes, I learned new ideas!", "It was very interesting!", "I am curious to learn more!", "All of the above!"],
                "correct_answer": 3,
                "explanation": "Every video is a stepping stone to new discoveries!"
            }
        ]
    }


async def generate_quiz(youtube_id: str, title: str, description: str = "") -> Optional[Dict[str, Any]]:
    """
    Generate a 3-question multiple-choice quiz based on the video's transcript.
    Falls back to title/description or default questions if transcript is not available.
    """
    if not settings.GEMINI_API_KEY:
        return _fallback_quiz(title)

    transcript_text = await _get_transcript(youtube_id)
    if transcript_text:
        source_context = f"Transcript Excerpt:\n{transcript_text[:4000]}"
    else:
        source_context = f"Description:\n{description[:1000] if description else 'An educational video about ' + title}"

    prompt = f"""You are a fun, friendly kids' trivia game host!
Create a quick 3-question playful "Brain Spark & Trivia Challenge" based on this video.
Make it super encouraging, playful, and fun for kids — NOT like a stressful school test.

Video Title: {title}
{source_context}

Return ONLY valid JSON with this exact structure:
{{
  "questions": [
    {{
      "question": "The fun question?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correct_answer": 0,
      "explanation": "Playful, celebratory explanation of the cool fact."
    }}
  ]
}}
Make the questions easy, entertaining, and age-appropriate for children. Ensure correct_answer is an integer (0, 1, 2, or 3).
"""
    try:
        import warnings
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            import google.generativeai as genai
        genai.configure(api_key=settings.GEMINI_API_KEY)
        model = genai.GenerativeModel("gemini-1.5-flash")
        response = model.generate_content(prompt)
        text = response.text.strip()
        json_match = re.search(r'\{.*\}', text, re.DOTALL)
        if json_match:
            return json.loads(json_match.group())
    except Exception as e:
        print(f"Quiz generation failed: {e}")
    
    return _fallback_quiz(title)


async def generate_curriculum(
    child_age: int,
    subject: str,
    goals: str = "",
    num_days: int = 5,
) -> Optional[Dict[str, Any]]:
    """
    Generate a multi-day learning curriculum tailored to a child's age and subject goals using Gemini.
    """
    if not settings.GEMINI_API_KEY:
        return {
            "subject": subject,
            "target_age": child_age,
            "total_days": num_days,
            "overview": f"A {num_days}-day wholesome story and creative discovery journey on {subject} for age {child_age}.",
            "days": [
                {
                    "day_number": i,
                    "topic": f"Story & Activity: {subject} - Day {i}",
                    "objective": f"Enjoy wholesome stories and creative fun about {subject}",
                    "search_queries": [f"{subject} stories for kids", f"{subject} animated cartoon for children"],
                    "follow_up_activity": "Share what you loved about today's story with your family."
                }
                for i in range(1, num_days + 1)
            ]
        }

    prompt = f"""You are a fun, creative kids' activity and discovery quest designer.
Design an exciting, joyful {num_days}-day Activity Quest for a {child_age}-year-old child.
This is NOT school homework or dry academic lecturing. It should be engaging, wholesome, hands-on, and fun!

Theme/Hobby/Curiosity: {subject}
Parent Focus: {goals if goals else 'Fun, hands-on, creative, inspiring activities'}

For each day, provide:
1. Day title (e.g. "Mission 1: Fold the Supersonic Glider", "Day 2: Secret Magic Science Tricks").
2. 1-sentence exciting hook for the child.
3. 2-3 specific, child-safe YouTube search queries that will find great follow-along videos (prefer Art Hub for Kids, CrunchLabs, SciShow Kids, Cosmic Kids, Paper Aviation, etc.).
4. A fun hands-on offline mini-challenge they can do right at home with everyday objects (paper, pencils, crayons, blocks, kitchen items).

Return ONLY valid JSON with this exact structure:
{{
  "subject": "{subject}",
  "target_age": {child_age},
  "total_days": {num_days},
  "overview": "Brief exciting 2-sentence summary of the fun adventure",
  "days": [
    {{
      "day_number": 1,
      "topic": "Mission Title",
      "objective": "What exciting thing the child explores or makes",
      "search_queries": ["query 1", "query 2"],
      "follow_up_activity": "Fun offline hands-on mini-challenge to do at home"
    }}
  ]
}}
"""
    try:
        import warnings
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            import google.generativeai as genai
        genai.configure(api_key=settings.GEMINI_API_KEY)
        model = genai.GenerativeModel("gemini-1.5-flash")
        response = model.generate_content(prompt)
        text = response.text.strip()
        json_match = re.search(r'\{.*\}', text, re.DOTALL)
        if json_match:
            return json.loads(json_match.group())
    except Exception as e:
        print(f"Curriculum generation failed: {e}")

    return {
        "subject": subject,
        "target_age": child_age,
        "total_days": num_days,
        "overview": f"A {num_days}-day guided exploration of {subject}.",
        "days": [
            {
                "day_number": i,
                "topic": f"{subject} Mastery - Step {i}",
                "objective": f"Explore {subject} concepts step-by-step",
                "search_queries": [f"{subject} for kids", f"learn {subject}"],
                "follow_up_activity": "Discuss what you watched with your parents."
            }
            for i in range(1, num_days + 1)
        ]
    }
