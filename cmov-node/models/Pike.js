//var Ticket = require('Ticket.js');
module.exports = function(sequelize, DataTypes) {
    var Pike = sequelize.define(
        'Pike',
        {
            email: {
                type: DataTypes.STRING,
                unique: true,
                allowNull: false,
                primaryKey: true
            },
            name: {
                type: DataTypes.STRING
            },
            picture: {
                type: DataTypes.STRING
            },
            token: {
                type: DataTypes.STRING(1000),
                unique: true
            },
            expireTime: {
                type: DataTypes.INTEGER
            }
        },
        {
            classMethods: {
                associate: function (models) {
                },
                addNewPike: function(pikeModel, email, name, authToken, expire) {
                    return pikeModel.findOrCreate({
                        where: {
                            email: email
                        }, defaults: {
                            name: name,
                            token: authToken,
                            expireTime: expire
                        }
                    });
                },
                findPikeWithToken: function(userModel, token) {
                    return userModel.findOne({
                        where: {
                            token: token
                        }
                    });
                }
            },
            tableName: 'pike',
            timestamps: false
        }
    );

    //User.hasMany(Ticket);

    return Pike;
};