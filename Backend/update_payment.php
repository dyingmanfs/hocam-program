<?php
header("Content-Type: application/json; charset=UTF-8");

include "db.php";

$slotId = $_POST["slotId"] ?? "";

if ($slotId == "") {
    echo json_encode([
        "success" => false,
        "message" => "Slot ID is required"
    ]);
    exit();
}

$sql = "UPDATE Slot 
        SET paymentInfo = 1 
        WHERE id = ?";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "SQL prepare failed: " . mysqli_error($conn)
    ]);
    exit();
}

mysqli_stmt_bind_param($stmt, "i", $slotId);

if (mysqli_stmt_execute($stmt)) {
    echo json_encode([
        "success" => true,
        "message" => "Payment marked as paid"
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