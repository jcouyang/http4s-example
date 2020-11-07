let env = env:APP_ENV

let dbHost = env:DB_HOST as Text

let dbPort = env:DB_PORT

let dbName = env:DB_NAME as Text

let dbUser = env:DB_USER as Text

let dbPass = env:DB_PASS as Text

in  { env
    , jokeService = "https://icanhazdadjoke.com"
    , database =
      { host = dbHost
      , port = dbPort
      , name = dbName
      , user = dbUser
      , pass = dbPass
      }
    }
