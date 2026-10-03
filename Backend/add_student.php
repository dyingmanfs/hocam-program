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
    "slotId",
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
$slotId = intval($_POST["slotId"]);
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
    First check:
    Is this slot owned by this teacher?
*/
$checkSlotSql = "SELECT Slot.id
                 FROM Slot
                 JOIN Lesson ON Slot.idLesson = Lesson.id
                 JOIN HasLesson ON Lesson.id = HasLesson.idLesson
                 WHERE Slot.id = ? AND HasLesson.idTeacher = ?";

$checkSlotStmt = $conn->prepare($checkSlotSql);

if (!$checkSlotStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Slot check prepare failed: " . $conn->error
    ]);
    exit;
}

$checkSlotStmt->bind_param("ii", $slotId, $teacherId);
$checkSlotStmt->execute();
$slotResult = $checkSlotStmt->get_result();

if ($slotResult->num_rows == 0) {
    echo json_encode([
        "success" => false,
        "message" => "This slot does not belong to this teacher"
    ]);
    exit;
}

$conn->begin_transaction();

try {
    /*
        Insert student
    */
    $sql = "INSERT INTO Student
            (id, predicted_lesson_hours, refaransName, hourlyWage, status, email, phoneNumber, fullName, idParent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    $stmt = $conn->prepare($sql);

    if (!$stmt) {
        throw new Exception("Student prepare failed: " . $conn->error);
    }

    $stmt->bind_param(
        "iisdssssi",
        $id,
        $predicted_lesson_hours,
        $refaransName,
        $hourlyWage,
        $status,
        $email,
        $phoneNumber,
        $fullName,
        $idParent
    );

    if (!$stmt->execute()) {
        throw new Exception("Insert student failed: " . $stmt->error);
    }

    /*
        Connect student to slot
        Check your HasSlot column names.
        If your table uses idSlot and idStudent, this is correct.
    */
    $hasSlotSql = "INSERT INTO HasSlot (idSlot, idStudent)
                   VALUES (?, ?)";

    $hasSlotStmt = $conn->prepare($hasSlotSql);

    if (!$hasSlotStmt) {
        throw new Exception("HasSlot prepare failed: " . $conn->error);
    }

    $hasSlotStmt->bind_param("ii", $slotId, $id);

    if (!$hasSlotStmt->execute()) {
        throw new Exception("HasSlot insert failed: " . $hasSlotStmt->error);
    }

    $conn->commit();

    echo json_encode([
        "success" => true,
        "message" => "Student added and connected to slot successfully"
    ]);

} catch (Exception $e) {
    $conn->rollback();

    echo json_encode([<?php
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

/*
    Reference Name is optional.
    So refaransName is NOT inside requiredFields.
*/
$requiredFields = [
    "teacherId",
    "slotId",
    "id",
    "predicted_lesson_hours",
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
$slotId = intval($_POST["slotId"]);
$id = intval($_POST["id"]);
$predicted_lesson_hours = intval($_POST["predicted_lesson_hours"]);

// Optional field
$refaransName = $_POST["refaransName"] ?? "";

$hourlyWage = floatval($_POST["hourlyWage"]);
$status = $_POST["status"];
$email = $_POST["email"];
$phoneNumber = $_POST["phoneNumber"];
$fullName = $_POST["fullName"];
$idParent = intval($_POST["idParent"]);

/*
    First check:
    Is this slot owned by this teacher?
*/
$checkSlotSql = "SELECT Slot.id
                 FROM Slot
                 JOIN Lesson ON Slot.idLesson = Lesson.id
                 JOIN HasLesson ON Lesson.id = HasLesson.idLesson
                 WHERE Slot.id = ? AND HasLesson.idTeacher = ?";

$checkSlotStmt = $conn->prepare($checkSlotSql);

if (!$checkSlotStmt) {
    echo json_encode([
        "success" => false,
        "message" => "Slot check prepare failed: " . $conn->error
    ]);
    exit;
}

$checkSlotStmt->bind_param("ii", $slotId, $teacherId);
$checkSlotStmt->execute();
$slotResult = $checkSlotStmt->get_result();

if ($slotResult->num_rows == 0) {
    echo json_encode([
        "success" => false,
        "message" => "This slot does not belong to this teacher"
    ]);
    exit;
}

$conn->begin_transaction();

try {
    /*
        Insert student
    */
    $sql = "INSERT INTO Student
            (id, predicted_lesson_hours, refaransName, hourlyWage, status, email, phoneNumber, fullName, idParent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    $stmt = $conn->prepare($sql);

    if (!$stmt) {
        throw new Exception("Student prepare failed: " . $conn->error);
    }

    $stmt->bind_param(
        "iisdssssi",
        $id,
        $predicted_lesson_hours,
        $refaransName,
        $hourlyWage,
        $status,
        $email,
        $phoneNumber,
        $fullName,
        $idParent
    );

    if (!$stmt->execute()) {
        throw new Exception("Insert student failed: " . $stmt->error);
    }

    /*
        Connect student to slot
    */
    $hasSlotSql = "INSERT INTO HasSlot (idSlot, idStudent)
                   VALUES (?, ?)";

    $hasSlotStmt = $conn->prepare($hasSlotSql);

    if (!$hasSlotStmt) {
        throw new Exception("HasSlot prepare failed: " . $conn->error);
    }

    $hasSlotStmt->bind_param("ii", $slotId, $id);

    if (!$hasSlotStmt->execute()) {
        throw new Exception("HasSlot insert failed: " . $hasSlotStmt->error);
    }

    $conn->commit();

    echo json_encode([
        "success" => true,
        "message" => "Student added and connected to slot successfully"
    ]);

} catch (Exception $e) {
    $conn->rollback();

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}

$conn->close();
?>