//var Ticket = require('Ticket.js');
module.exports = function(sequelize, DataTypes) {
    var User = sequelize.define(
        'User',
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
                    User.hasMany(models.Ticket);
                },
                findAllUsers: function(userModel) {
                    return userModel.findAll({
                        where: {
                            email: "aa@aa.aa"
                        }
                    });
                },
                addNewUser: function(userModel, email, name, authToken, expire) {
                    return userModel.create({
                        email: email,
                        name: name,
                        token: authToken,
                        expireTime: expire
                    });
                },
                findUserWithToken: function(userModel, token) {
                    return userModel.findOne({
                        where: {
                            token: token
                        }
                    });
                }
            },
            tableName: 'user',
            timestamps: false
        }
    );

    //User.hasMany(Ticket);

    return User;
};