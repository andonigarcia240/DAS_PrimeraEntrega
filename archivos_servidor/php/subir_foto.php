<?php
require_once 'config.php';

error_log("=== SUBIR FOTO LLAMADO ===");
error_log("POST: " . print_r($_POST, true));
error_log("FILES: " . print_r($_FILES, true));

$user_id = $_POST['user_id'] ?? '';

if (empty($user_id)) {
    error_log("ERROR: user_id vacio");
    echo json_encode(['success' => false, 'message' => 'Falta user_id']);
    exit;
}

if (!isset($_FILES['foto'])) {
    error_log("ERROR: no hay archivo foto");
    echo json_encode(['success' => false, 'message' => 'No se recibio foto']);
    exit;
}

if ($_FILES['foto']['error'] !== UPLOAD_ERR_OK) {
    error_log("ERROR upload: " . $_FILES['foto']['error']);
    echo json_encode(['success' => false, 'message' => 'Error upload: ' . $_FILES['foto']['error']]);
    exit;
}

$directorio = '/var/www/html/gamelog/fotos/';
if (!file_exists($directorio)) {
    mkdir($directorio, 0755, true);
}

$extension = pathinfo($_FILES['foto']['name'], PATHINFO_EXTENSION);
$nombre_archivo = 'user_' . $user_id . '_' . time() . '.' . $extension;
$ruta = $directorio . $nombre_archivo;
$ruta_relativa = 'fotos/' . $nombre_archivo;

error_log("Intentando mover a: " . $ruta);

if (move_uploaded_file($_FILES['foto']['tmp_name'], $ruta)) {
    $stmt = $conn->prepare("UPDATE usuarios SET foto_perfil = ? WHERE id = ?");
    $stmt->bind_param("si", $ruta_relativa, $user_id);
    $stmt->execute();
    $stmt->close();
    error_log("Foto subida correctamente: " . $ruta_relativa);
    echo json_encode(['success' => true, 'foto_url' => $ruta_relativa]);
} else {
    error_log("ERROR: move_uploaded_file fallo");
    echo json_encode(['success' => false, 'message' => 'Error al mover archivo']);
}

$conn->close();
?>
