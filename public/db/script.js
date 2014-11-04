conn = new Mongo();
db = conn.getDB("eventual");

db.dropDatabase()

db.createCollection('tweets')

db.tweets.insert({
	text: 'Tweet de Prueba 1',
    posivote: NumberInt(0),
    neuvote: NumberInt(0),
    negvote: NumberInt(0),
    user: ''
})

db.tweets.insert({
	text: 'Tweet de Prueba 2',
    posivote: NumberInt(0),
    neuvote: NumberInt(0),
    negvote: NumberInt(0),
    user: ''
})

db.tweets.insert({
	text: 'Tweet de Prueba 3',
    posivote: NumberInt(0),
    neuvote: NumberInt(0),
    negvote: NumberInt(0),
    user: ''
})

db.tweets.insert({
	text: 'Tweet de Prueba 4',
    posivote: NumberInt(0),
    neuvote: NumberInt(0),
    negvote: NumberInt(0),
    user: ''
})

db.tweets.insert({
	text: 'Tweet de Prueba 5',
    posivote: NumberInt(0),
    neuvote: NumberInt(0),
    negvote: NumberInt(0),
    user: ''
})

db.tweets.insert({
	text: 'Tweet de Prueba 6',
	posivote: NumberInt(0),
	neuvote: NumberInt(0),
	negvote: NumberInt(0),
	user: ''
})
