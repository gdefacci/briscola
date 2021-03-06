export const testData2 = {
  "http://localhost:8080/app/games/1": {
    "isLastHandTurn": false,
    "players": [
      "http://localhost:8080/app/players/2",
      "http://localhost:8080/app/players/1"
    ],
    "currentPlayer": "http://localhost:8080/app/players/2",
    "isLastGameTurn": false,
    "moves": [
    ],
    "nextPlayers": [
      "http://localhost:8080/app/players/2",
      "http://localhost:8080/app/players/1"
    ],
    "self": "http://localhost:8080/app/games/1",
    "briscolaCard": {
      "number": 10,
      "seed": "coppe",
      "points": 4
    },
    "deckCardsNumber": 34,
    "playerState": "http://localhost:8080/app/games/1/player/2",
    "kind": "active"
  },
  "http://localhost:8080/app/players/2": {
    "self": "http://localhost:8080/app/players/2",
    "name": "Pippo"
  },
  "http://localhost:8080/app/players/1": {
    "self": "http://localhost:8080/app/players/1",
    "name": "Minni"
  },
  "http://localhost:8080/app/players/3": {
    "self": "http://localhost:8080/app/players/2",
    "name": "Jane"
  },
  "http://localhost:8080/app/players/4": {
    "self": "http://localhost:8080/app/players/1",
    "name": "Joe"
  },
  "http://localhost:8080/app/games/1/player/2": {
    "player": "http://localhost:8080/app/players/2",
    "cards": [
      {
        "number": 6,
        "seed": "coppe",
        "points": 0
      },
      {
        "number": 1,
        "seed": "bastoni",
        "points": 11
      },
      {
        "number": 6,
        "seed": "spade",
        "points": 0
      }
    ],
    "score": {
      "cards": []
    }
  },
  "http://localhost:8080/app/competitions/1": {
    "decliningPlayers": [],
    "accept": null,
    "self": "http://localhost:8080/app/competitions/1",
    "acceptingPlayers": [
      "http://localhost:8080/app/players/2"
    ],
    "competition": {
      "players": [
        "http://localhost:8080/app/players/1",
        "http://localhost:8080/app/players/2"
      ],
      "kind": {
        "kind": "singleMatch"
      },
      "deadline": {
        "kind": "allPlayers"
      }
    },
    "kind": "open",
    "decline": null
  },
  "http://localhost:8080/app/competitions/1/player/1": {
    "decliningPlayers": [],
    "accept": "http://localhost:8080/app/competitions/1/player/1/accept",
    "self": "http://localhost:8080/app/competitions/1",
    "acceptingPlayers": [
      "http://localhost:8080/app/players/2"
    ],
    "competition": {
      "players": [
        "http://localhost:8080/app/players/1",
        "http://localhost:8080/app/players/2"
      ],
      "kind": {
        "kind": "singleMatch"
      },
      "deadline": {
        "kind": "allPlayers"
      }
    },
    "kind": "open",
    "decline": "http://localhost:8080/app/competitions/1/player/1/decline"
  }
}

export const eventAndState1 = {
  "event": {
    "game": {
      "isLastHandTurn": false,
      "players": [
        "http://localhost:8080/app/players/2",
        "http://localhost:8080/app/players/1"
      ],
      "currentPlayer": "http://localhost:8080/app/players/2",
      "isLastGameTurn": false,
      "moves": [
      ],
      "nextPlayers": [
        "http://localhost:8080/app/players/2",
        "http://localhost:8080/app/players/1"
      ],
      "self": "http://localhost:8080/app/games/1",
      "briscolaCard": {
        "number": 5,
        "seed": "spade",
        "points": 0
      },
      "deckCardsNumber": 34,
      "playerState": "http://localhost:8080/app/games/1/player/2",
      "kind": "active"
    },
    "kind": "gameStarted"
  },
  "state": {
    "isLastHandTurn": false,
    "players": [
      "http://localhost:8080/app/players/2",
      "http://localhost:8080/app/players/1"
    ],
    "currentPlayer": "http://localhost:8080/app/players/2",
    "isLastGameTurn": false,
    "moves": [
    ],
    "nextPlayers": [
      "http://localhost:8080/app/players/2",
      "http://localhost:8080/app/players/1"
    ],
    "self": "http://localhost:8080/app/games/1",
    "briscolaCard": {
      "number": 5,
      "seed": "spade",
      "points": 0
    },
    "deckCardsNumber": 34,
    "playerState": "http://localhost:8080/app/games/1/player/2",
    "kind": "active"
  }
}

export const competionStartEvent = {
  "event": {
    "issuer": "http://localhost:8080/app/players/2",
    "competition": "http://localhost:8080/app/competitions/1/player/1",
    "kind": "createdCompetition"
  },
  "state": {
    "decliningPlayers": [],
    "accept": "http://localhost:8080/app/competitions/1/player/1/accept",
    "self": "http://localhost:8080/app/competitions/1",
    "acceptingPlayers": [
      "http://localhost:8080/app/players/2"
    ],
    "competition": {
      "players": [
        "http://localhost:8080/app/players/1",
        "http://localhost:8080/app/players/2"
      ],
      "kind": {
        "kind": "singleMatch"
      },
      "deadline": {
        "kind": "allPlayers"
      }
    },
    "kind": "open",
    "decline": "http://localhost:8080/app/competitions/1/player/1/decline"
  }
}

export const finishedGameState = {
  "state": {
    "self": "http://localhost:8080/app/games/1",
    "briscolaCard": {
      "number": 3,
      "seed": "coppe",
      "points": 10
    },
    "gameResult": {
      "playersOrderByPoints": [
        {
          "player": "http://localhost:8080/app/players/1",
          "points": 37,
          "score": {
            "cards": [
              {
                "number": 7,
                "seed": "bastoni",
                "points": 0
              },
              {
                "number": 3,
                "seed": "coppe",
                "points": 10
              },
              {
                "number": 10,
                "seed": "coppe",
                "points": 4
              },
              {
                "number": 6,
                "seed": "denari",
                "points": 0
              },
              {
                "number": 1,
                "seed": "denari",
                "points": 11
              },
              {
                "number": 3,
                "seed": "bastoni",
                "points": 10
              },
              {
                "number": 8,
                "seed": "denari",
                "points": 2
              },
              {
                "number": 2,
                "seed": "coppe",
                "points": 0
              }
            ]
          }
        },
        {
          "player": "http://localhost:8080/app/players/3",
          "points": 42,
          "score": {
            "cards": [
              {
                "number": 8,
                "seed": "bastoni",
                "points": 2
              },
              {
                "number": 4,
                "seed": "coppe",
                "points": 0
              },
              {
                "number": 4,
                "seed": "spade",
                "points": 0
              },
              {
                "number": 1,
                "seed": "coppe",
                "points": 11
              },
              {
                "number": 1,
                "seed": "bastoni",
                "points": 11
              },
              {
                "number": 8,
                "seed": "coppe",
                "points": 2
              },
              {
                "number": 2,
                "seed": "denari",
                "points": 0
              },
              {
                "number": 6,
                "seed": "bastoni",
                "points": 0
              },
              {
                "number": 8,
                "seed": "spade",
                "points": 2
              },
              {
                "number": 1,
                "seed": "spade",
                "points": 11
              },
              {
                "number": 9,
                "seed": "coppe",
                "points": 3
              },
              {
                "number": 7,
                "seed": "denari",
                "points": 0
              }
            ]
          }
        },
        {
          "player": "http://localhost:8080/app/players/2",
          "points": 22,
          "score": {
            "cards": [
              {
                "number": 5,
                "seed": "bastoni",
                "points": 0
              },
              {
                "number": 10,
                "seed": "bastoni",
                "points": 4
              },
              {
                "number": 6,
                "seed": "spade",
                "points": 0
              },
              {
                "number": 3,
                "seed": "denari",
                "points": 10
              },
              {
                "number": 2,
                "seed": "spade",
                "points": 0
              },
              {
                "number": 2,
                "seed": "bastoni",
                "points": 0
              },
              {
                "number": 10,
                "seed": "spade",
                "points": 4
              },
              {
                "number": 5,
                "seed": "denari",
                "points": 0
              },
              {
                "number": 4,
                "seed": "denari",
                "points": 0
              },
              {
                "number": 10,
                "seed": "denari",
                "points": 4
              },
              {
                "number": 5,
                "seed": "spade",
                "points": 0
              },
              {
                "number": 6,
                "seed": "coppe",
                "points": 0
              }
            ]
          }
        },
        {
          "player": "http://localhost:8080/app/players/4",
          "points": 19,
          "score": {
            "cards": [
              {
                "number": 3,
                "seed": "spade",
                "points": 10
              },
              {
                "number": 7,
                "seed": "coppe",
                "points": 0
              },
              {
                "number": 5,
                "seed": "coppe",
                "points": 0
              },
              {
                "number": 9,
                "seed": "spade",
                "points": 3
              },
              {
                "number": 4,
                "seed": "bastoni",
                "points": 0
              },
              {
                "number": 7,
                "seed": "spade",
                "points": 0
              },
              {
                "number": 9,
                "seed": "bastoni",
                "points": 3
              },
              {
                "number": 9,
                "seed": "denari",
                "points": 3
              }
            ]
          }
        }
      ],
      "winner": {
        "player": "http://localhost:8080/app/players/1",
        "points": 37,
        "score": {
          "cards": [
            {
              "number": 7,
              "seed": "bastoni",
              "points": 0
            },
            {
              "number": 3,
              "seed": "coppe",
              "points": 10
            },
            {
              "number": 10,
              "seed": "coppe",
              "points": 4
            },
            {
              "number": 6,
              "seed": "denari",
              "points": 0
            },
            {
              "number": 1,
              "seed": "denari",
              "points": 11
            },
            {
              "number": 3,
              "seed": "bastoni",
              "points": 10
            },
            {
              "number": 8,
              "seed": "denari",
              "points": 2
            },
            {
              "number": 2,
              "seed": "coppe",
              "points": 0
            }
          ]
        }
      }
    },
    "kind": "finished"
  }
}