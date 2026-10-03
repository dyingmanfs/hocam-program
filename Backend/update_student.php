<?php
header("Content-Type: application/json; charset=UTF-8");

$host = getenv("DB_HOST") ?: "127.0.0.1";
$user = getenv("DB_USER") ?: "root";
$password = getenv("DB_PASSWORD") ?: "";
$dbname = getenv("DB_NAME") ?: "hocamprogram";
$port = (int) (getenv("DB_PORT") ?: 3306);

$conn = new mysqli($host, $user, $password, $dbname, $port);

if ($conn->connect_error) {
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit;
}

$requiredFields = [
    "teacherId",
    "id",
    "predicted_lesson_hours",
    "refaransName",
    "hourlyWage",
    "status",
    "email",
    "phoneNumber",
    "fullName",
    "idParent"
];

foreach ($requiredFields as $field) {
    if (!isset($_POST[$field])) {
        echo json_encode([
            "success" => false,
            "message" => "Missing field: " . $field
        ]);
        exit;
    }
}

$teacherId = intval($_POST["teacherId"]);
$id = intval($_POST["id"]);
$predicted_lesson_hours = intval($_POST["predicted_lesson_hours"]);
$refaransName = $_POST["refaransName"];
$hourlyWage = floatval($_POST["hourlyWage"]);
$status = $_POST["status"];
$email = $_POST["email"];
$phoneNumber = $_POST["phoneNumber"];
$fullName = $_POST["fullName"];
$idParent = intval($_POST["idParent"]);

/*
    Check:
    Does this student belong to this teacher?
*/
$checkSql = "SELECT Student.id
             FROM Student
             JOIN HasSlot ON Student.id = HasSlot.idStudent
             JOIN Slot ON HasSlot.idSlot = Slot.id
             JOIN Lesson ON Slot.idLesson = Lesson.id
             JOIN HasLesson ON Lesson.id = HasLesson.idLesson
             WHERE Student.id = ? AND HasLesson.idTeacher = ?
             LIMIT 1";

$checkStmt = $conn->prepare($checkSql);

if (!$checkStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Check prepare failed: " . $conn->error
    ]);
    exit;
}

$checkStmt->bind_param("ii", $id, $teacherId);
$checkStmt->execute();
$result = $checkStmt->get_result();

if ($result->num_rows == 0) {
    echo json_encode([
        "success" => false,
        "message" => "This student does not belong to this teacher"
    ]);
    exit;
}

$sql = "UPDATE Student
        SET predicted_lesson_hours = ?,
            refaransName = ?,
            hourlyWage = ?,
            status = ?,
            email = ?,
            phoneNumber = ?,
            fullName = ?,
            idParent = ?
        WHERE id = ?";

$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "Prepare failed: " . $conn->error
    ]);
    exit;
}

$stmt->bind_param(
    "isdssssii",
    $predicted_lesson_hours,
    $refaransName,
    $hourlyWage,
    $status,
    $email,
    $phoneNumber,
    $fullName,
    $idParent,
    $id
);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode([
            "success" => true,
            "message" => "Student updated successfully"
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "No changes made"
        ]);
    }
} else {
    echo json_encode([
        "success" => false,
        "message" => "Update failed: " . $stmt->error
    ]);
}

$stmt->close();
$conn->close();
?>