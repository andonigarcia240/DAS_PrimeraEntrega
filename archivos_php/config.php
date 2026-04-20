<?php
define('DB_HOST', 'localhost');
define('DB_USER', 'gameloguser');
define('DB_PASS', '1234');
define('DB_NAME', 'gamelog');

$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

if ($conn->connect_error) {
    die(json_encode(['success' => false, 'message' => 'Error de conexión']));
}

$conn->set_charset('utf8');
?>
