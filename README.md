# Three Card Poker — CS 342 Project 2

A two-player Three Card Poker game in JavaFX. Both players take turns on the same screen (hot-seat) and each one plays against the dealer.

## Requirements
- JDK 25
- Maven 3.9+

## Build and run
```bash
mvn clean javafx:run
```

## Run the tests
```bash
mvn test
```
There are 57 JUnit tests: 30 for `ThreeCardLogic`, 14 for `Deck`/`Dealer`/`Card`, and 13 for `GameService` (round flow and wager checks).

## Project layout
```
src/main/java/edu/uic/cs342/poker/
├── Main.java                 starts the app
├── model/                    Card, Deck, Dealer, Player
├── logic/                    ThreeCardLogic (hand ranks, payouts, compare)
├── service/                  GameService, Phase, PlayerResult
├── navigation/               SceneManager (screens + theme), Dialogs (quit + rules)
└── controller/               WelcomeController, GameController, ResultsController,
                              CardView, Animations
src/main/resources/edu/uic/cs342/poker/
├── fxml/                     welcome.fxml, game.fxml, results.fxml
└── css/                      light.css, dark.css
docs/UML_ClassDiagram.pdf     class diagram of the final code
```

## Main components
| Part | Classes | What it does |
|---|---|---|
| Model | `Card`, `Deck`, `Dealer`, `Player` | Cards (suit + value 2–14), a 52-card deck that shuffles and resets, the dealer who deals 3-card hands, and each player's hand, bets and total winnings. |
| Game rules | `ThreeCardLogic` | Static methods only. `evalHand` ranks a hand, `evalPPWinnings` pays the Pair Plus bet, `compareHands` picks the winner (with tie-breaks), plus `dealerQualifies` and `handName`. |
| Game flow | `GameService`, `Phase`, `PlayerResult` | Runs one round: bets → deal → Play/Fold for each player → reveal → payouts. Checks that bets are $5–$25, blocks actions in the wrong phase, carries the Ante over when the dealer does not qualify, and updates total winnings. |
| Controllers | `WelcomeController`, `GameController`, `ResultsController` | Handle clicks, call `GameService`, turn buttons on and off for the current phase, and update the screen. They never rank hands or calculate payouts. |
| Navigation | `SceneManager`, `Dialogs` | Loads each FXML screen, keeps the current game, switches the light/dark theme, and shows the Quit confirmation and Rules popups. |

### Return values (kept from the original project)
- `evalHand`: 0 = high card, 1 = straight flush, 2 = three of a kind, 3 = straight, 4 = flush, 5 = pair
- `evalPPWinnings`: the amount won on the Pair Plus bet (bet × odds), or 0 if it lost
- `compareHands(dealer, player)`: 0 = tie, 1 = dealer wins, 2 = player wins

### Rules used
- Ante $5–$25. Pair Plus is optional ($5–$25). Play = Ante.
- Fold loses the Ante and Pair Plus.
- Dealer needs Queen-high or better. If the dealer does not qualify, the Play bet is returned and the Ante stays on the table for the next hand.
- If the dealer qualifies: player wins → Ante and Play pay 1:1; dealer wins → both are lost; tie → both are returned.
- Pair Plus pays 40:1, 30:1, 6:1, 3:1, 1:1 (straight flush down to pair), no matter what the dealer has.
- A-2-3 counts as a straight (the lowest one).

## Screens
| Screen | Purpose |
|---|---|
| Welcome (`welcome.fxml`) | Title, Start Game, How to Play, Exit, theme switch and a "Reduce motion" option. |
| Game (`game.fxml`) | The whole round. A phase tracker (Bet, Deal, Decide, Reveal, Payout), a status bar that says what to do next, the dealer's cards, and one panel per player with cards, bets, Play/Fold and total winnings. Results show in a popup that closes with a click anywhere. |
| Results (`results.fxml`) | The dealer's hand and whether it qualified, a winner banner, a table with each hand, each bet's payout and the new totals, and Play Again / Main Menu. |

Other popups: Quit confirmation (Keep Playing is the default) and How to Play.

## Animations
| Animation | Where | Why |
|---|---|---|
| Deal | Game screen, after Deal Cards | Cards slide in one at a time (Player 1, Player 2, Dealer, repeat), so players see who got which cards. |
| Dealer flip | Game screen, after Reveal Dealer | Each dealer card turns over one by one, so the reveal is easy to follow before the result shows. |
| Win emphasis | Result popup and Results banner | The winning box and the winner banner grow for a moment, so the eye goes to who won first. |
| Screen fade | Every screen change | The new screen fades in, so moving between screens feels connected. |

With "Reduce motion" turned on (Welcome screen) nothing moves. Every result is still written out in text, so the game is just as clear without animation.

## Themes
`light.css` holds all the styles and defines every color once. `dark.css` only changes those colors. The "Switch to Dark / Switch to Light" button on every screen swaps them.

## Changes from Part 1 usability testing
1. **"Click anywhere to continue" on result popups.** Testers did not know how to close the result popup. Every popup now says how to dismiss it, and a click or any key closes it.
2. **Total winnings easier to see.** Testers had trouble seeing their total winnings because the text was small and gray. The text is now dark and the amount is 20 px bold.
3. **Play field looks read-only.** An AI walkthrough pointed out that the Play field looked like an input box even though Play must equal the Ante. It is now shown as a gray amount instead of a text field.

## Design reflection

**1. Why did you separate game logic from the FXML controllers?**
So the rules have one home. `ThreeCardLogic` and `GameService` know nothing about JavaFX, and the controllers only ask `GameService` to do something and then redraw. If a payout is wrong, there is one place to look, and the controllers stay short.

**2. What usability problems did other students identify?**
They did not know how to close the result popup, and they had trouble seeing their total winnings because the text was small and gray.

**3. Which wireframe revisions most improved the user experience?**
"Click anywhere to continue" helped the most, because without it players got stuck on the popup with no way forward. Making total winnings bigger helped players keep track of the score between rounds.

**4. How do the animations help users understand game events?**
The deal shows the order cards go out and who owns them. The flip makes the dealer reveal a clear moment instead of a sudden change. The win emphasis points to the winner. Screen fades show that you moved to a new screen.

**5. How could the interface be redesigned without changing the game rules?**
Everything on screen lives in the FXML, CSS and controllers. The layout, colors or even the whole screen order could change (for example, one screen per player) while `ThreeCardLogic` and `GameService` stay exactly the same. The light and dark themes already show this: they look different and use the same code.

**6. How does your architecture make the application easier to test and maintain?**
The rules and the round flow are plain Java, so JUnit can test every hand rank, payout, tie-break and phase change without opening a window. A test can also give `GameService` a dealer with a fixed deck to check exact outcomes. Changing the UI cannot break those tests, and changing a rule only touches one class.

## AI assistance
Parts of this project's code and documentation were written with help from Claude (AI), as allowed by the course.
