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
            isPike: {
                type: DataTypes.BOOLEAN,
                allowNull: false
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
                }
            },
            tableName: 'user',
            timestamps: false
        }
    );

    //User.hasMany(Ticket);

    return User;
};