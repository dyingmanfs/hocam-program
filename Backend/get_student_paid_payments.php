<?php
header("Content-Type: application/json; charset=UTF-8");
include "db.php";

$teacherId = $_GET["teacherId"] ?? "";
$studentName = $_GET["studentName"] ?? "";

if ($teacherId == "" || $studentName == "") {
    echo json_encode([
        "success" => false,
        "message" => "Teacher ID and student name are required"
    ]);
    exit();
}

$searchName = "%" . $studentName . "%";

$sql = "
SELECT
    Student.fullName,
    Lesson.LessonName,
    Slot.slotDate,
    Slot.duration,
    Student.hourlyWage,
    Slot.paymentInfo,
    (Student.hourlyWage * Slot.duration / 60) AS amount
FROM Teacher
JOIN HasLesson ON Teacher.id = HasLesson.idTeacher
JOIN Lesson ON HasLesson.idLesson = Lesson.id
JOIN Slot ON Lesson.id = Slot.idLesson
JOIN HasSlot ON Slot.id = HasSlot.idSlot
JOIN Student ON HasSlot.idStudent = Student.id
WHERE Teacher.id = ?
AND Student.fullName LIKE ?
AND Slot.paymentInfo = 1
ORDER BY Student.fullName, Slot.slotDate ASC
";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "SQL prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

mysqli_stmt_bind_param($stmt, "is", $teacherId, $searchName);
mysqli_stmt_execute($stmt);

$result = mysqli_stmt_get_result($stmt);

$payments = [];

while ($row = mysqli_fetch_assoc($result)) {
    $payments[] = [
        "fullName" => $row["fullName"],
        "LessonName" => $row["LessonName"],
        "slotDate" => $row["slotDate"],
        "duration" => (int)$row["duration"],
        "hourlyWage" => (float)$row["hourlyWage"],
        "paymentInfo" => (int)$row["paymentInfo"],
        "amount" => (float)$row["amount"]
    ];
}

echo json_encode([
    "success" => true,
    "payments" => $payments
]);

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>