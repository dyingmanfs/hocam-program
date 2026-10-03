<?php
header("Content-Type: application/json; charset=UTF-8");
include "db.php";

$teacherId = $_GET["teacherId"] ?? "";

if ($teacherId == "") {
    echo json_encode([
        "success" => false,
        "message" => "Teacher ID is required"
    ]);
    exit();
}

$sql = "
SELECT DISTINCT
    Student.id,
    Student.fullName
FROM Student
JOIN HasSlot ON Student.id = HasSlot.idStudent
JOIN Slot ON HasSlot.idSlot = Slot.id
JOIN Lesson ON Slot.idLesson = Lesson.id
JOIN HasLesson ON Lesson.id = HasLesson.idLesson
WHERE HasLesson.idTeacher = ?
ORDER BY Student.fullName ASC
";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "SQL prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

mysqli_stmt_bind_param($stmt, "i", $teacherId);
mysqli_stmt_execute($stmt);

$result = mysqli_stmt_get_result($stmt);

$students = [];

while ($row = mysqli_fetch_assoc($result)) {
    $students[] = $row;
}

echo json_encode([
    "success" => true,
    "students" => $students
]);

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>