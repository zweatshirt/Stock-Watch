# Stock-Watch
CS442 Stock Watch Application Project

- Still need to test when lack of connection.
- Also never had a chance to test my app while market was actually open...

- Implements multi-threading
- Single activity application that uses a RecyclerView to maintain a user list of stocks.
- SQLite implementation to save stock info for the user upon onDestory, populate in onCreate
- Easy conversion to JSON parsing. I personally thought JSON was a better option for this
  project.
- API usage to collect stock data. Hopefully nobody abuses my access token lol.

If I ever pick this up again in the future:
  - Implement connection pooling for stock updates
  - Idk I want to make an application that uses AI to determine effective stocks to purchase.
    Literally everyone has this idea though.
