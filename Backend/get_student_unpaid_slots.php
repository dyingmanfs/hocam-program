<?php
header("Content-Type: application/json; charset=UTF-8");

include "db.php";

$teacherId = $_GET["teacherId"] ?? "";
$studentId = $_GET["studentId"] ?? "";

if ($teacherId == "" || $studentId == "") {
    echo json_encode([
        "success" => false,
        "message" => "Teacher ID and Student ID are required"
    ]);
    exit();
}

$sql = "SELECT
    Slot.id AS slotId,
    Student.fullName,
    Lesson.LessonName,
    Slot.slotDate,
    Slot.duration,
    Student.hourlyWage,
    (Student.hourlyWage * Slot.duration / 60) AS amount,
    Slot.paymentInfo
FROM Teacher
JOIN HasLesson ON Teacher.id = HasLesson.idTeacher
JOIN Lesson ON HasLesson.idLesson = Lesson.id
JOIN Slot ON Lesson.id = Slot.idLesson
JOIN HasSlot ON Slot.id = HasSlot.idSlot
JOIN Student ON HasSlot.idStudent = Student.id
WHERE Teacher.id = ?
  AND Student.id = ?
  AND Slot.paymentInfo = 0
ORDER BY Slot.slotDate";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "SQL prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

mysqli_stmt_bind_param($stmt, "ii", $teacherId, $studentId);
mysqli_stmt_execute($stmt);

$result = mysqli_stmt_get_result($stmt);

$slotList = [];

while ($row = mysqli_fetch_assoc($result)) {
    $slotList[] = $row;
}

echo json_encode([
    "success" => true,
    "slotList" => $slotList
]);

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>