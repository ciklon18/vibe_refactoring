const mysql = require('mysql');

const connection = mysql.createConnection({
    host: process.env.MYSQL_HOST || 'localhost',
    user: process.env.MYSQL_USER || 'mysql',
    password: process.env.MYSQL_PASSWORD || 'mysql',
    database: process.env.MYSQL_DATABASE || 'db'
});

module.exports = { connection };
