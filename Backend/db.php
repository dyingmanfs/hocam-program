<?php
header("Content-Type: application/json; charset=UTF-8");

$host = getenv("DB_HOST") ?: "127.0.0.1";
$username = getenv("DB_USER") ?: "root";
$password = getenv("DB_PASSWORD") ?: "";
$database = getenv("DB_NAME") ?: "hocamprogram";
$port = (int) (getenv("DB_PORT") ?: 3306);

$conn = mysqli_connect($host, $username, $password, $database, $port);

if (!$conn) {
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . mysqli_connect_error()
    ]);
    exit();
}

mysqli_set_charset($conn, "utf8");
?>