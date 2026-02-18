
const mysql = require('mysql2');
require('dotenv').config();

const db = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: '123456', 
    database: 'app_tshusuarios'
});

module.exports = db.promise(); // Usamos promesas para un código más limpio
