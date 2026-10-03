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
SELECT 
    Teacher.fullName,
    Lesson.id AS lessonId,
    Lesson.LessonName,
    Lesson.Type
FROM Teacher
JOIN HasLesson ON Teacher.id = HasLesson.idTeacher
JOIN Lesson ON HasLesson.idLesson = Lesson.id
WHERE Teacher.id = ?
ORDER BY Lesson.LessonName
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

$lessonList = [];

while ($row = mysqli_fetch_assoc($result)) {
    $lessonList[] = $row;
}

echo json_encode([
    "success" => true,
    "lessonList" => $lessonList
]);

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>