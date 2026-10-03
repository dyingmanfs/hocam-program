<?php
header("Content-Type: application/json; charset=UTF-8");

include "db.php";

$teacherId = $_GET["teacherId"];

$sql = "SELECT 
    Student.id,
    Student.fullName,
    SUM(Student.hourlyWage * Slot.duration / 60) AS unpaidDebt
FROM Teacher
JOIN HasLesson ON Teacher.id = HasLesson.idTeacher
JOIN Lesson ON HasLesson.idLesson = Lesson.id
JOIN Slot ON Lesson.id = Slot.idLesson
JOIN HasSlot ON Slot.id = HasSlot.idSlot
JOIN Student ON HasSlot.idStudent = Student.id
WHERE Teacher.id = ? 
  AND Slot.paymentInfo = 0
GROUP BY Student.id, Student.fullName
HAVING unpaidDebt > 0";

$stmt = mysqli_prepare($conn, $sql);
mysqli_stmt_bind_param($stmt, "i", $teacherId);
mysqli_stmt_execute($stmt);

$result = mysqli_stmt_get_result($stmt);

if (!$result) {
    echo json_encode([
        "success" => false,
        "message" => "Query failed: " . mysqli_error($conn)
    ]);
    exit();
}

$paymentList = [];

while ($row = mysqli_fetch_assoc($result)) {
    $paymentList[] = $row;
}

echo json_encode([
    "success" => true,
    "paymentList" => $paymentList
]);

mysqli_close($conn);
?>