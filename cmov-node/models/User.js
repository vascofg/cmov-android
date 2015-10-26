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
            gToken: {
                type: DataTypes.STRING(1000),
                unique: true
            },
            token: {
                type: DataTypes.STRING(1000),
                unique: true
            }
        },
        {
            tableName: 'user',
            timestamps: false
        }
    );

    return User;
};