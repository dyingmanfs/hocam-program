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

if (!isset($_POST["id"]) || !isset($_POST["teacherId"])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing field: id or teacherId"
    ]);
    exit;
}

$id = intval($_POST["id"]);
$teacherId = intval($_POST["teacherId"]);

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

$conn->begin_transaction();

try {
   
    $deleteHasSlotSql = "DELETE HasSlot
                         FROM HasSlot
                         JOIN Slot ON HasSlot.idSlot = Slot.id
                         JOIN Lesson ON Slot.idLesson = Lesson.id
                         JOIN HasLesson ON Lesson.id = HasLesson.idLesson
                         WHERE HasSlot.idStudent = ?
                         AND HasLesson.idTeacher = ?";

    $deleteHasSlotStmt = $conn->prepare($deleteHasSlotSql);

    if (!$deleteHasSlotStmt) {
        throw new Exception("HasSlot delete prepare failed: " . $conn->error);
    }

    $deleteHasSlotStmt->bind_param("ii", $id, $teacherId);

    if (!$deleteHasSlotStmt->execute()) {
        throw new Exception("HasSlot delete failed: " . $deleteHasSlotStmt->error);
    }

    /*
        Then delete student.
        If the same student can belong to more than one teacher, do NOT delete from Student.
        In that case, only delete HasSlot relation.
    */
    $deleteStudentSql = "DELETE FROM Student WHERE id = ?";

    $deleteStudentStmt = $conn->prepare($deleteStudentSql);

    if (!$deleteStudentStmt) {
        throw new Exception("Student delete prepare failed: " . $conn->error);
    }

    $deleteStudentStmt->bind_param("i", $id);

    if (!$deleteStudentStmt->execute()) {
        throw new Exception("Student delete failed: " . $deleteStudentStmt->error);
    }

    if ($deleteStudentStmt->affected_rows > 0) {
        $conn->commit();

        echo json_encode([
            "success" => true,
            "message" => "Student deleted successfully"
        ]);
    } else {
        throw new Exception("No student found with this ID");
    }

} catch (Exception $e) {
    $conn->rollback();

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}

$conn->close();
?>