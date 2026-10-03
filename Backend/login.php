<?php
header("Content-Type: application/json; charset=UTF-8");

include "db.php";

$email = $_POST["email"] ?? "";
$password = $_POST["password"] ?? "";

if ($email == "" || $password == "") {
    echo json_encode([
        "success" => false,
        "message" => "Email and password are required"
    ]);
    exit();
}

$sql = "SELECT id, fullName, email 
        FROM Teacher 
        WHERE email = ? AND password = ?";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "SQL prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

mysqli_stmt_bind_param($stmt, "ss", $email, $password);
mysqli_stmt_execute($stmt);

$result = mysqli_stmt_get_result($stmt);

if (mysqli_num_rows($result) > 0) {
    $teacher = mysqli_fetch_assoc($result);

    echo json_encode([
        "success" => true,
        "message" => "Login successful",
        "teacher" => $teacher
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Wrong email or password"
    ]);
}

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>