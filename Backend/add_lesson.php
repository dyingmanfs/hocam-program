<?php
header("Content-Type: application/json; charset=UTF-8");

include "db.php";

$teacherId = $_POST["teacherId"] ?? "";
$lessonName = $_POST["lessonName"] ?? "";
$type = $_POST["type"] ?? "";

if ($teacherId == "" || $lessonName == "" || $type == "") {
    echo json_encode([
        "success" => false,
        "message" => "Teacher ID, lesson name and type are required"
    ]);
    exit();
}

mysqli_begin_transaction($conn);

try {
    $sql1 = "INSERT INTO Lesson (LessonName, Type) VALUES (?, ?)";
    $stmt1 = mysqli_prepare($conn, $sql1);

    if (!$stmt1) {
        throw new Exception("Lesson prepare failed: " . mysqli_error($conn));
    }

    mysqli_stmt_bind_param($stmt1, "ss", $lessonName, $type);

    if (!mysqli_stmt_execute($stmt1)) {
        throw new Exception("Lesson insert failed: " . mysqli_stmt_error($stmt1));
    }

    $lessonId = mysqli_insert_id($conn);

    if ($lessonId == 0) {
        throw new Exception("Lesson ID was not generated. Check Lesson.id AUTO_INCREMENT.");
    }

    $sql2 = "INSERT INTO HasLesson (idTeacher, idLesson) VALUES (?, ?)";
    $stmt2 = mysqli_prepare($conn, $sql2);

    if (!$stmt2) {
        throw new Exception("HasLesson prepare failed: " . mysqli_error($conn));
    }

    mysqli_stmt_bind_param($stmt2, "ii", $teacherId, $lessonId);

    if (!mysqli_stmt_execute($stmt2)) {
        throw new Exception("HasLesson insert failed: " . mysqli_stmt_error($stmt2));
    }

    mysqli_commit($conn);

    echo json_encode([
        "success" => true,
        "message" => "Lesson added successfully",
        "lessonId" => $lessonId
    ]);

} catch (Exception $e) {
    mysqli_rollback($conn);

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}

mysqli_close($conn);
?>