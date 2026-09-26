# SmartPantry – Smart Pantry Manager

SmartPantry is a Java Android app that helps reduce food waste. You record the
ingredients you have at home, and the app suggests recipes you can cook
**using only those ingredients**. A recipe is only suggested when every
ingredient it needs is in your pantry, in at least the required amount.

Built for **Mobile App Development 700** (Richfield).

## Features

- **Pantry management (full CRUD):** add, view, edit and delete ingredients with
  a name, quantity, unit and optional expiry date. The form validates every field.
- **Pantry list:** a RecyclerView with a custom adapter, bound to the database
  through LiveData so it updates automatically.
- **18 seeded recipes:** loaded into the database automatically on first launch.
- **Suggested Recipes (strict matching):** shows only recipes where nothing is
  missing. A separate **Almost there** tab lists recipes missing exactly one
  ingredient, kept apart from the strict suggestions.
- **Recipe detail:** full ingredient list and method.
- **Settings:** switch expiring-soon alerts on or off and choose how many days
  ahead count as "expiring soon". Expiring items are highlighted in red.
- **Empty states:** clear messages when the pantry is empty or no recipes match.

## Screens

| Screen | Activity |
|---|---|
| Pantry List (home) | `MainActivity` |
| Add / Edit Ingredient | `AddEditPantryActivity` |
| Suggested Recipes | `SuggestedRecipesActivity` |
| Recipe Detail | `RecipeDetailActivity` |
| Settings | `SettingsActivity` |

Navigation uses the toolbar menu on the Pantry List ("What can I cook?" and
Settings in the overflow menu), with Intents passing item and recipe IDs
between screens.

## Database: SQLite with Room

The app stores its data locally on the device in **SQLite**, using the
**Room** persistence library. There are three tables:

- `pantry_items` – id, name, quantity, unit, expiry_date
- `recipes` – id, name, instructions
- `recipe_ingredients` – id, recipe_id (foreign key → recipes.id), ingredient_name, required_quantity, unit

**Why SQLite/Room:**

- The pantry belongs to one user on one phone, so there is no need for a server
  or internet connection. The app works fully offline.
- Room checks SQL queries at compile time and gives LiveData that updates the UI
  automatically when data changes.
- Data persists after the app is closed and reopened, with no account or setup
  needed.
- It matches the persistent data approach covered in the module.

Small user preferences (the Settings screen) are stored in SharedPreferences.

## The strict-matching rule

`RecipeMatcher` decides which recipes qualify. Before comparing, it:

1. **Normalises names:** lower case, extra spaces removed, plurals made singular
   ("Tomatoes" → "tomato"), and a few synonyms mapped ("mayo" → "mayonnaise").
2. **Converts units** to a base unit: grams for mass (g, kg) and millilitres for
   volume (ml, l, tsp, tbsp, cup). Count units (unit, clove, slice, can) must
   match exactly.
3. **Adds up duplicate pantry entries** for the same ingredient.

A recipe is "Ready to cook" only if **every** ingredient is covered. If two
amounts can't be compared (e.g. grams vs cups), the ingredient counts as missing.

Unit tests for this logic are in `app/src/test/.../RecipeMatcherTest.java`.

## How to run

1. Install **Android Studio** (a recent stable version) with the Android SDK.
2. Clone the repository:
   ```
   git clone https://github.com/TeazerCRL/SmartPantry.git
   ```
3. In Android Studio choose **File → Open** and select the `SmartPantry` folder.
4. Wait for **Gradle sync** to finish (the first sync downloads dependencies).
5. Choose an emulator or a connected Android phone (USB debugging on),
   then click **Run ▶**.
6. To run the unit tests: right-click `RecipeMatcherTest` → **Run**.

Minimum Android version: 7.0 (API 24).

## Tech

- Java, Android Studio, Gradle (Kotlin DSL)
- Room (SQLite), LiveData, RecyclerView, Material Components
- No Google Maps, location or GPS features are used.
