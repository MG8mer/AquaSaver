# AquaSaver

AquaSaver is an Android app for tracking household water usage and helping users build better conservation habits. 

The project was motivated by water scarcity in places like Jordan. Instead of only giving users general conservation advice, AquaSaver lets them log specific activities, estimate how much water they used, and immediately see how that usage compares with their conservation goal.

## Features

- Log water usage from activities such as showers, washing machines, sprinklers, car washing, cleaning, and gardening
- Estimate water consumption in liters based on the activity and time spent
- Track progress against daily, weekly, or monthly water goals
- Visualize current usage with interactive charts
- Undo the most recent water usage entry
- Create user profiles with configurable conservation goals
- Track goal streaks and completed conservation challenges
- View conservation tips and suggestions
- Generate usage reports in the background
- Store user, goal, challenge, report, and water usage data persistently
- Support optional location-based settings and weather alerts

## Tech Stack

**Language:** Java  
**Platform:** Android  
**UI:** Android Views, Fragments, View Binding  
**Data:** Room, Firebase Firestore  
**Charts:** MPAndroidChart  
**Background Tasks:** Android WorkManager  
**Location:** Google Play Services Location API  
**Build System:** Gradle

## How It Works

The main screen is centered around logging water usage.

A user selects an activity, enters how long they spent doing it, and AquaSaver estimates the amount of water used based on that activity's expected flow rate. The new entry is saved and the user's progress is recalculated against their current daily, weekly, or monthly goal.

The app keeps separate models for data such as:

- `UserProfile`
- `WaterUsage`
- `GoalProgress`
- `Reports`
- `Challenges`
- `ChallengeProgress`
- `Suggestions`
- `Alerts`

The project was originally built around a Room persistence layer with entities, DAOs, migrations, and repository classes. We later began moving parts of the data layer toward Firebase Firestore for cloud-backed storage.

Background work is handled with Android WorkManager. For example, the app schedules report generation independently of the foreground UI.

## Project Structure

```text
app/src/main/java/com/example/aquasaver/
├── challenges/        # Conservation challenge logic
├── db/                # Database configuration and converters
├── model/             # Core application data models
├── repository/        # Data access layer
└── ui/
    ├── home/          # Water logging and goal visualization
    ├── goals/         # Goals, challenges, and streaks
    ├── water_usage/   # Usage history and reporting
    ├── conservation_tips/
    ├── settings/
    └── main_pages/    # Login, signup, navigation, and workers
