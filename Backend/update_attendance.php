<?php
header("Content-Type: application/json; charset=UTF-8");
include "db.php";

$slotId = $_POST["slotId"] ?? "";
$attendance = $_POST["attendance"] ?? "";

if ($slotId == "" || $attendance == "") {
    echo json_encode([
        "success" => false,
        "message" => "slotId and attendance are required"
    ]);
    exit();
}

$sql = "UPDATE Slot SET attendance = ? WHERE id = ?";
$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "Prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

mysqli_stmt_bind_param($stmt, "ii", $attendance, $slotId);

if (mysqli_stmt_execute($stmt)) {
    echo json_encode([
        "success" => true,
        "message" => "Attendance updated"
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Update failed: " . mysqli_error($conn)
    ]);
}

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>