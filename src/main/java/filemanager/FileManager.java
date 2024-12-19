package filemanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;

public class FileManager {

	public static <T> void saveFile(T data, String filePath) {
		// Khởi tạo Gson với tùy chọn setPrettyPrinting để định dạng JSON
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (Writer writer = new BufferedWriter(new FileWriter(filePath))) {
			gson.toJson(data, writer);
		} catch (IOException e) {
			System.err.println("Lỗi khi lưu file: " + filePath);
			e.printStackTrace();
		}
	}


	public static <T> T loadFile(String filePath, Type typeOfCollection) {
		File file = new File(filePath);

		// Kiểm tra nếu file không tồn tại, tạo file với dữ liệu mặc định
		if (!file.exists()) {
			System.out.println("Chưa có file " +filePath);
			return null;
		}
		try (Reader reader = new BufferedReader(new FileReader(filePath))) {
			Gson gson = new Gson();
			return gson.fromJson(reader, typeOfCollection);
		} catch (IOException e) {
			System.err.println("Lỗi khi đọc file: " + filePath);
			e.printStackTrace();
		}
		return null;
	}

	public static <T> void saveObject(T obj, String fileName, Type typeOfCollection) {
		// Tải danh sách đối tượng từ file
		LinkedHashSet<T> objectList = loadFile(fileName, typeOfCollection);
		// Nếu file chưa tồn tại hoặc null, tạo mới danh sách
		if (objectList == null) {
			objectList = new LinkedHashSet<>();
		}
		// Thêm đối tượng mới vào danh sách
		objectList.add(obj);
		// Lưu danh sách vào file
		saveFile(objectList, fileName);
		// In thông báo kết quả
		System.out.println("Size: " + objectList.size());
		System.out.println("Lưu đối tượng " + obj.toString() + " vào " + fileName + " thành công");
	}

	public static void moveFileToFolder(String filePath, String folderPath) {
		File file = new File(filePath);
		File folder = new File(folderPath);

		if (!file.exists()) {
			System.err.println("Tệp không tồn tại: " + filePath);
			return;
		}

		if (!folder.exists()) {
			folder.mkdirs();  // Tạo thư mục nếu chưa có
		}

		File destination = new File(folder, file.getName());

		if (destination.exists()) {
			if (destination.delete()) {
				System.out.println("Đã xóa tệp cũ: " + destination.getPath());
			} else {
				System.err.println("Không thể xóa tệp cũ: " + destination.getPath());
				return;
			}
		}

		if (file.renameTo(destination)) {
			System.out.println("Đã di chuyển tệp " + filePath + " đến " + folderPath);
		} else {
			System.err.println("Không thể di chuyển tệp: " + filePath);
		}
	}

	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			if (file.delete()) {
				System.out.println("Đã xóa tệp: " + filePath);
			} else {
				System.err.println("Không thể xóa tệp: " + filePath);
			}
		} else {
			System.out.println("Tệp không tồn tại: " + filePath);
			return;
		}
	}

}