<?php
header("Content-Type: application/json; charset=UTF-8");
include "db.php";

$teacherId = $_GET["teacherId"] ?? "";
$date = $_GET["date"] ?? "";

if ($teacherId == "") {
    echo json_encode([
        "success" => false,
        "message" => "Teacher ID is required"
    ]);
    exit();
}

$sql = "
SELECT
    Teacher.fullName AS teacherName,
    Lesson.LessonName,
    Student.fullName AS studentName,
    Slot.id AS slotId,
    DATE(Slot.slotDate) AS slotDay,
    TIME(Slot.slotDate) AS slotTime,
    Slot.slotDate,
    Slot.duration,
    Slot.attendance,
    Slot.paymentInfo,
    Slot.period
FROM Teacher
JOIN HasLesson ON Teacher.id = HasLesson.idTeacher
JOIN Lesson ON HasLesson.idLesson = Lesson.id
JOIN Slot ON Lesson.id = Slot.idLesson
JOIN HasSlot ON Slot.id = HasSlot.idSlot
JOIN Student ON HasSlot.idStudent = Student.id
WHERE Teacher.id = ?
";

if ($date != "") {
    $sql .= " AND DATE(Slot.slotDate) = ? ";
}

$sql .= " ORDER BY Slot.slotDate";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "SQL prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

if ($date != "") {
    mysqli_stmt_bind_param($stmt, "is", $teacherId, $date);
} else {
    mysqli_stmt_bind_param($stmt, "i", $teacherId);
}

mysqli_stmt_execute($stmt);

$result = mysqli_stmt_get_result($stmt);

if (!$result) {
    echo json_encode([
        "success" => false,
        "message" => "Query failed: " . mysqli_error($conn)
    ]);
    exit();
}

$slots = [];

while ($row = mysqli_fetch_assoc($result)) {
    $slots[] = [
        "teacherName" => $row["teacherName"],
        "LessonName" => $row["LessonName"],
        "studentName" => $row["studentName"],
        "slotId" => (int)$row["slotId"],
        "slotDay" => $row["slotDay"],
        "slotTime" => $row["slotTime"],
        "slotDate" => $row["slotDate"],
        "duration" => (int)$row["duration"],
        "attendance" => (int)$row["attendance"],
        "paymentInfo" => (int)$row["paymentInfo"],
        "period" => $row["period"]
    ];
}

echo json_encode([
    "success" => true,
    "slots" => $slots
]);

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>