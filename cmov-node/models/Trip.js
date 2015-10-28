module.exports = function(sequelize, DataTypes) {
    var Trip = sequelize.define(
        'Trip',
        {
            id: {
                type: DataTypes.INTEGER,
                unique: true,
                allowNull: false,
                primaryKey: true
            }
        },
        {
            tableName: 'ticket',
            timestamps: false
        }
    );
    //
    //Trip.hasMany(Ticket);

    return Trip;
};