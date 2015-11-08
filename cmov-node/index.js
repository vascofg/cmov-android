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
                database: 'cmov',
                user: 'root',
                pass: 'root',
                dialect: 'mysql',
                host: '127.0.0.1',
                port: 3306,
                models: 'models/**/*.js',
                sequelize: {
                    define: {
                        underscoredAll: true
                    }
                }
            }
        },
        {
            'hapi-auth-bearer-simple' : {

            }
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

var validateFunction = function (token, callback) {

    // Use a real strategy here to check if the token is valid
    //if (token === 'abc456789') {
    //    callback(null, true, userCredentials);
    //}
    //else {
    //    callback(null, false, userCredentials);
    //}

    console.log("validate");
    console.log(token);
    callback(null, false, {ok:'ok'});
};

Glue.compose(manifest, options, function (err, server) {

    if (err) {
        throw err;
    }

    console.log('syncing');
    var db = server.plugins['hapi-sequelized'].db;
    db.sequelize.sync({ force: true }).then(function () {
        console.log('models synced');
    });

    server.auth.strategy('userAuth', 'bearerAuth', {
        validateFunction: validateFunction
    });

    var routes = require('./routes/routes.js');

    server.route(routes);

    server.start(function () {

        console.log('Hapi days!');
    });
});