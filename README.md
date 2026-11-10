# Expense Tracker

An Android app for tracking expenses grouped under user-defined notes, with live totals, an archive, and a recoverable "Recently Deleted" bin. Built with Kotlin, Room and Coroutines/Flow.

## Features

- **Notes with expenses:** create notes (e.g., "Goa Trip", "Monthly Groceries") and add expense entries under each one
- **Live totals:** running totals update instantly as expenses are added, edited or removed
- **Archive:** move finished notes out of the main list without losing them
- **Soft delete:** deleted notes go to *Recently Deleted* and are kept for 30 days; older items are cleaned up automatically on app launch
- **Navigation Drawer** for moving between Notes, Archive and Recently Deleted
- **Works offline:** all data is stored locally on the device

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | XML layouts, RecyclerView, Navigation Drawer |
| Local database | Room (SQLite) with KSP |
| Async / reactive | Kotlin Coroutines and Flow |
| Architecture | MVVM |
| Build | Gradle (Kotlin DSL) |

## How It Works

- Each **note** has many **expenses**, modelled with a one-to-many foreign-key relation in Room.
- The UI observes Room queries as `Flow`, so the RecyclerView and totals refresh automatically when data changes.
- Deleting a note sets a deletion timestamp instead of removing the row. On launch, a cleanup step permanently removes notes deleted more than 30 days ago.


## Future Improvements

- Charts and category-wise spending summary
- Export to CSV or PDF
- Search and filters
- Cloud backup
