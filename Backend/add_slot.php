<?php
header("Content-Type: application/json; charset=UTF-8");
include "db.php";

$studentId = $_POST["studentId"] ?? "";
$lessonId = $_POST["lessonId"] ?? "";
$slotDate = $_POST["slotDate"] ?? "";
$duration = $_POST["duration"] ?? "";
$period = $_POST["period"] ?? "";

if ($studentId == "" || $lessonId == "" || $slotDate == "" || $duration == "" || $period == "") {
    echo json_encode([
        "success" => false,
        "message" => "All fields are required"
    ]);
    exit();
}

mysqli_begin_transaction($conn);

try {
    $sqlSlot = "
    INSERT INTO Slot 
    (slotDate, duration, attendance, paymentInfo, period, idLesson)
    VALUES (?, ?, 0, 0, ?, ?)
    ";

    $stmtSlot = mysqli_prepare($conn, $sqlSlot);

    if (!$stmtSlot) {
        throw new Exception("Slot prepare failed: " . mysqli_error($conn));
    }

    mysqli_stmt_bind_param($stmtSlot, "sisi", $slotDate, $duration, $period, $lessonId);

    if (!mysqli_stmt_execute($stmtSlot)) {
        throw new Exception("Slot insert failed: " . mysqli_stmt_error($stmtSlot));
    }

    $slotId = mysqli_insert_id($conn);

    $sqlHasSlot = "
    INSERT INTO HasSlot
    (idSlot, idStudent)
    VALUES (?, ?)
    ";

    $stmtHasSlot = mysqli_prepare($conn, $sqlHasSlot);

    if (!$stmtHasSlot) {
        throw new Exception("HasSlot prepare failed: " . mysqli_error($conn));
    }

    mysqli_stmt_bind_param($stmtHasSlot, "ii", $slotId, $studentId);

    if (!mysqli_stmt_execute($stmtHasSlot)) {
        throw new Exception("HasSlot insert failed: " . mysqli_stmt_error($stmtHasSlot));
    }

    mysqli_commit($conn);

    echo json_encode([
        "success" => true,
        "message" => "Slot added successfully"
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