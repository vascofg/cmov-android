var Glue = require('glue');

var manifest = {
    server: {},
    connections: [
        {
            port: 3000,
            host: 'localhost'
        }
    ],
    plugins: [
        {
            'hapi-sequelized': {
                database: 'dbltcocs5l7m41',
                user: 'kyaiteeygnwzve',
                pass: 'XEk8haSjZ6PbsXc34AkewgGUMw',
                dialect: 'postgres',
                host: 'ec2-54-247-170-228.eu-west-1.compute.amazonaws.com',
                port: 5432,
                //uri: 'mysql://ba47ac6b0083d9:d35e2bbd@eu-cdbr-west-01.cleardb.com/heroku_be3348d54cc3478?reconnect=true',
                //uri: 'postgres://kyaiteeygnwzve:XEk8haSjZ6PbsXc34AkewgGUMw@ec2-54-247-170-228.eu-west-1.compute.amazonaws.com:5432/dbltcocs5l7m41',
                //uri: 'postgres://kyaiteeygnwzve:XEk8haSjZ6PbsXc34AkewgGUMw@ec2-54-247-170-228.eu-west-1.compute.amazonaws.com:5432/dbltcocs5l7m41?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory',
                models: 'models/**/*.js',
                sequelize: {
                    define: {
                        underscoredAll: true
                    },
                    dialectOptions: {
                        ssl: true
                    }
                }

            }
        },
        {
            'hapi-auth-bearer-simple': {}
        }
        //{
        //    'bell': {
        //
        //    }
        //}
    ]
};

var options = {
    relativeTo: __dirname
};

var validateUser = function (token, request, callback) {

    var models = request.server.plugins['hapi-sequelized'].db.sequelize.models;

    models.User.findUserWithToken(models.User, token).then(function (user) {

            var date = Math.floor(Date.now() / 1000);

            //console.log(user.dataValues);
            if (date <= user.expireTime) {
                console.log("Valid token");
                callback(null, true, user.dataValues);
            } else {
                console.log("INVALID TOKEN");
                callback({Error: "Invalid token"}, false, null);
            }
        })
        .catch(function (error) {
            console.log(error);
            console.log("ERROR IN DATABASE");

            callback({Error: "Internal Error - user not found"}, false, null);
        });
};

var validatePike = function (token, request, callback) {

    var models = request.server.plugins['hapi-sequelized'].db.sequelize.models;

    models.Pike.findPikeWithToken(models.Pike, token).then(function (user) {

            var date = Math.floor(Date.now() / 1000);

            //console.log(user.dataValues);
            if (date <= user.expireTime) {
                console.log("Valid token");
                callback(null, true, user.dataValues);
            } else {
                console.log("INVALID TOKEN");
                callback({Error: "Invalid token"}, false, null);
            }
        })
        .catch(function (error) {
            console.log(error);
            console.log("ERROR IN DATABASE");

            callback({Error: "Internal Error - user not found"}, false, null);
        });
};

Glue.compose(manifest, options, function (err, server) {

    if (err) {
        throw err;
    }

    console.log('syncing');
    var db = server.plugins['hapi-sequelized'].db;
    db.sequelize.sync({force: true}).then(function () {
        console.log('models synced');
    });

    server.auth.strategy('userAuth', 'bearerAuth', {
        validateFunction: validateUser,
        exposeRequest: true
    });

    var routes = require('./routes/routes.js');

    server.route(routes);

    server.start(function () {

        console.log('Hapi days!');
    });
});