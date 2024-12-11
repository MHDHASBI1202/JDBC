import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TreeMap;

// Kelas Barang
class Barang {
    protected String kodeBarang;
    protected String namaBarang;
    protected double hargaBarang;
    protected int jumlahStok;

    public Barang(String kodeBarang, String namaBarang, double hargaBarang, int jumlahStok) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaBarang = hargaBarang;
        this.jumlahStok = jumlahStok;
    }

    public double hitungTotal(int jumlahBeli) {
        return hargaBarang * jumlahBeli;
    }

    public int getJumlahStok() {
        return jumlahStok; 
    }

    public void kurangiStok(int jumlahBeli) {
        this.jumlahStok -= jumlahBeli;
    }

    @Override
    public String toString() {
        return "Nama Barang: " + namaBarang + "\n" +
               "Kode Barang: " + kodeBarang + "\n" +
               "Harga Barang: " + hargaBarang + "\n" +
               "Jumlah Stok: " + jumlahStok;
    }
}

// Kelas Transaksi yang merupakan turunan dari Barang
class Transaksi extends Barang {
    private String noFaktur;
    private int jumlahBeli;
    private double total;

    public Transaksi(String noFaktur, String kodeBarang, String namaBarang, double hargaBarang, int jumlahBeli) {
        super(kodeBarang, namaBarang, hargaBarang, 0); 
        this.noFaktur = noFaktur;
        this.jumlahBeli = jumlahBeli;
        this.total = hitungTotal(jumlahBeli);
    }

    @Override
    public String toString() {
        return "No. Faktur: " + noFaktur + "\n" +
               "Kode Barang: " + kodeBarang + "\n" +
               "Nama Barang: " + namaBarang + "\n" +
               "Harga Barang: " + hargaBarang + "\n" +
               "Jumlah Beli: " + jumlahBeli + "\n" +
               "Total: " + total;
    }
}

// Kelas Admin untuk mengelola barang
class Admin {
    private TreeMap<String, Barang> barangMap;
    private Connection connection; // Variabel untuk koneksi database

    public Admin() throws SQLException {
        barangMap = new TreeMap<>();
        // Connect to the PostgreSQL database
        String url = "jdbc:postgresql://localhost:1202/JDBC";
        String user = "postgres"; // ganti dengan username PostgreSQL Anda
        String password = "Naynay12"; // ganti dengan password PostgreSQL Anda
        connection = DriverManager.getConnection(url, user, password);
        
        // Memuat semua barang dari database ke dalam TreeMap
        loadAllBarangFromDatabase();
    }

    private void loadAllBarangFromDatabase() throws SQLException {
        String sql = "SELECT * FROM barang";
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String kodeBarang = resultSet.getString("kode_barang");
                String namaBarang = resultSet.getString("nama_barang");
                double hargaBarang = resultSet.getDouble("harga_barang");
                int jumlahStok = resultSet.getInt("jumlah_stok");
                barangMap.put(kodeBarang, new Barang(kodeBarang, namaBarang, hargaBarang, jumlahStok));
            }
        }
    }

    public void tambahBarang(String kodeBarang, String namaBarang, double hargaBarang, int jumlahStok) throws Exception {
        if (barangMap.containsKey(kodeBarang)) {
            throw new Exception("\nBarang dengan kode " + kodeBarang + " sudah ada.");
        }
        barangMap.put(kodeBarang, new Barang(kodeBarang, namaBarang, hargaBarang, jumlahStok));
        saveBarangToDatabase(kodeBarang, namaBarang, hargaBarang, jumlahStok);
        System.out.println("\nBarang berhasil ditambahkan.");
    }

    private void saveBarangToDatabase(String kodeBarang, String namaBarang, double hargaBarang, int jumlahStok) throws SQLException {
        String sql = "INSERT INTO barang (kode_barang, nama_barang, harga_barang, jumlah_stok) VALUES (?, ?, ?, ?)";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, kodeBarang);
            preparedStatement.setString(2, namaBarang);
            preparedStatement.setDouble(3, hargaBarang);
            preparedStatement.setInt(4, jumlahStok);
            preparedStatement.executeUpdate();
        }
    }

    public void hapusBarang(String kodeBarang) throws Exception {
        if (!barangMap.containsKey(kodeBarang)) {
            throw new Exception("\nBarang dengan kode " + kodeBarang + " tidak ditemukan.");
        }
        barangMap.remove(kodeBarang);
        deleteBarangFromDatabase(kodeBarang);
        System.out.println("\nBarang berhasil dihapus.");
    }

    private void deleteBarangFromDatabase(String kodeBarang) throws SQLException {
        String sql = "DELETE FROM barang WHERE kode_barang = ?";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, kodeBarang);
            preparedStatement.executeUpdate();
        }
    }

    public void ubahBarang(String kodeBarang, String namaBarang, double hargaBarang, int jumlahStok) throws Exception {
        if (!barangMap.containsKey(kodeBarang)) {
            throw new Exception("\nBarang dengan kode " + kodeBarang + " tidak ditemukan.");
        }
        barangMap.put(kodeBarang, new Barang(kodeBarang, namaBarang, hargaBarang, jumlahStok));
        updateBarangInDatabase(kodeBarang, namaBarang, hargaBarang, jumlahStok);
        System.out.println("\nBarang berhasil diubah.");
    }

    private void updateBarangInDatabase(String kodeBarang, String namaBarang, double hargaBarang, int jumlahStok) throws SQLException {
        String sql = "UPDATE barang SET nama_barang = ?, harga_barang = ?, jumlah_stok = ? WHERE kode_barang = ?";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, namaBarang);
            preparedStatement.setDouble(2, hargaBarang);
            preparedStatement.setInt(3, jumlahStok);
            preparedStatement.setString(4, kodeBarang);
            preparedStatement.executeUpdate();
        }
    }

    public void cariBarang(String kodeBarang) {
        Barang barang = barangMap.get(kodeBarang);
        if (barang != null) {
            System.out.println("\n----------------------------------------------------");
            System.out.println(barang);
        } else {
            // Jika barang tidak ditemukan di TreeMap, coba ambil dari database
            try {
                Barang barangFromDb = getBarangFromDatabase(kodeBarang);
                if (barangFromDb != null) {
                    System.out.println("\nBarang ditemukan di database:");
                    System.out.println(barangFromDb);
                } else {
                    System.out.println("\nBarang dengan kode " + kodeBarang + " tidak ditemukan.");
                }
            } catch (SQLException e) {
                System.out.println("Gagal mengambil barang dari database: " + e.getMessage());
            }
        }
    }

    private Barang getBarangFromDatabase(String kodeBarang) throws SQLException {
        String sql = "SELECT * FROM barang WHERE kode_barang = ?";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, kodeBarang);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String namaBarang = resultSet.getString("nama_barang");
                double hargaBarang = resultSet.getDouble("harga_barang");
                int jumlahStok = resultSet.getInt("jumlah_stok");
                return new Barang(kodeBarang, namaBarang, hargaBarang, jumlahStok);
            }
        }
        return null; // Jika tidak ditemukan
    }

    public void tampilkanSemuaBarang() {
        if (!barangMap.isEmpty()) { 
            for (Barang barang : barangMap.values()) {
                System.out.println("\n----------------------------------------------------");
                System.out.println(barang);
                System.out.println("----------------------------------------------------");
            }
        } else {
            System.out.println("\nTidak ada barang yang tersedia.");
            try {
                displayAllBarangFromDatabase();
            } catch (SQLException e) {
                System.out.println("Gagal mengambil barang dari database: " + e.getMessage());
            }
        }
    }

    private void displayAllBarangFromDatabase() throws SQLException {
        String sql = "SELECT * FROM barang";
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String kodeBarang = resultSet.getString("kode_barang");
                String namaBarang = resultSet.getString("nama_barang");
                double hargaBarang = resultSet.getDouble("harga_barang");
                int jumlahStok = resultSet.getInt("jumlah_stok");
                System.out.println(new Barang(kodeBarang, namaBarang, hargaBarang, jumlahStok));
            }
        }
    }

    public Barang getBarang(String kodeBarang) {
        // Coba ambil dari TreeMap
        Barang barang = barangMap.get(kodeBarang);
        if (barang == null) {
            // Jika tidak ditemukan di TreeMap, coba ambil dari database
            try {
                barang = getBarangFromDatabase(kodeBarang);
                if (barang != null) {
                    // Jika ditemukan di database, tambahkan ke TreeMap untuk akses cepat di masa depan
                    barangMap.put(kodeBarang, barang);
                }
            } catch (SQLException e) {
                System.out.println("Gagal mengambil barang dari database: " + e.getMessage());
            }
        }
        return barang;
    }

    // Pastikan untuk menutup koneksi saat tidak lagi digunakan
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}

// Kelas Utama dengan method main
class Tes {
    private static int fakturCounter = 1;
    private static Random random = new Random();
    private static Admin admin;

    public static void main(String[] args) {
        // Menggunakan try-with-resources untuk Scanner
        try (Scanner scanner = new Scanner(System.in)) {
            try {
                admin = new Admin(); // Inisialisasi Admin dan koneksi database
            } catch (SQLException e) {
                System.out.println("Gagal terhubung ke database: " + e.getMessage());
                return;
            }

            while (true) {
                // Menu Pilihan Login
                System.out.println("+-----------------------------------------------------+");
                System.out.println("         SISTEM INFORMASI SUPERMARKET SOKLARIS         ");
                System.out.println("+-----------------------------------------------------+");
                System.out.println("1. Login sebagai Admin");
                System.out.println("2. Login sebagai Kasir");
                System.out.println("3. Keluar Aplikasi");
                System.out.print("Pilih menu login (1/2/3): ");
                
                String pilihanLogin = scanner.nextLine();
                
                switch (pilihanLogin) {
                    case "1":
                        if (loginAdmin(scanner)) {
                            menuAdmin(scanner);
                        }
                        break;
                    
                    case "2":
                        if (loginKasir(scanner)) {
                            prosesTransaksi(scanner);
                        }
                        break;
                    
                    case "3":
                        System.out.println("Terima kasih telah menggunakan program ini. Sampai jumpa!");
                        admin.closeConnection(); // Menutup koneksi sebelum keluar
                        return;
                    
                    default:
                        System.out.println("Pilihan tidak valid. Silakan coba lagi.");
                }
            }
        } // Scanner akan otomatis ditutup di sini
    }


    private static boolean loginAdmin(Scanner scanner) {
        String username = "admin";
        String password = "admin123";
        String captcha = generateCaptcha();

        System.out.println("+-----------------------------------------------------+");
        System.out.println("                   LOGIN ADMIN                        ");
        System.out.println("+-----------------------------------------------------+");
        
        System.out.print("Username: ");
        String inputUsername = scanner.nextLine().trim();

        System.out.print("Password: ");
        String inputPassword = scanner.nextLine().trim();

        System.out.println("Captcha: " + captcha);
        System.out.print("Masukkan Captcha: ");
        String inputCaptcha = scanner.nextLine().trim();

        if (inputUsername.equalsIgnoreCase(username) && 
            inputPassword.equals(password) && 
            inputCaptcha.equalsIgnoreCase(captcha)) {
            System.out.println("\nLogin Admin berhasil.");
            return true;
        } else {
            System.out.println("\nLogin Admin gagal. Silakan coba lagi.");
            return false;
        }
    }

    private static boolean loginKasir(Scanner scanner) {
        String username = "kasir";
        String password = "kasir123";
        String captcha = generateCaptcha();

        System.out.println("+-----------------------------------------------------+");
        System.out.println("                   LOGIN KASIR                        ");
        System.out.println("+-----------------------------------------------------+");
        
        System.out.print("Username: ");
        String inputUsername = scanner.nextLine().trim();

        System.out.print("Password: ");
        String inputPassword = scanner.nextLine().trim();

        System.out.println("Captcha: " + captcha);
        System.out.print("Masukkan Captcha: ");
        String inputCaptcha = scanner.nextLine().trim();

        if (inputUsername.equalsIgnoreCase(username) && 
            inputPassword.equals(password) && 
            inputCaptcha.equalsIgnoreCase(captcha)) {
            System.out.println("\nLogin Kasir berhasil.");
            return true;
        } else {
            System.out.println("\nLogin Kasir gagal. Silakan coba lagi.");
            return false;
        }
    }

    private static void menuAdmin(Scanner scanner) {
        while (true) {
            System.out.println("\n+-----------------------------------------------------+");
            System.out.println("                   MENU ADMIN                         ");
            System.out.println("+-----------------------------------------------------+");
            System.out.println("1. Tambah Barang");
            System.out.println("2. Hapus Barang");
            System.out.println("3. Ubah Barang");
            System.out.println("4. Cari Barang");
            System.out.println("5. Tampilkan Semua Barang");
            System.out.println("6. Keluar");
            System.out.print("Pilih menu (1-6): ");
            String pilihan = scanner.nextLine();

            switch (pilihan) {
                case "1":
                    try {
                        System.out.print("Masukkan Kode Barang: ");
                        String kodeBarang = scanner.nextLine().trim();
                        System.out.print("Masukkan Nama Barang: ");
                        String namaBarang = scanner.nextLine().trim();
                        System.out.print("Masukkan Harga Barang: ");
                        double hargaBarang = scanner.nextDouble();
                        System.out.print("Masukkan Jumlah Stok: ");
                        int jumlahStok = scanner.nextInt();
                        scanner.nextLine(); // Clear buffer
                        admin.tambahBarang(kodeBarang, namaBarang, hargaBarang, jumlahStok);
                    } catch (Exception e) {
                        System.out.println("Terjadi kesalahan: " + e.getMessage());
                    }
                    break;
                case "2":
                    try {
                        System.out.print("Masukkan Kode Barang yang ingin dihapus: ");
                        String kodeBarang = scanner.nextLine().trim();
                        admin.hapusBarang(kodeBarang);
                    } catch (Exception e) {
                        System.out.println("Terjadi kesalahan: " + e.getMessage());
                    }
                    break;

                case "3":
                    try {
                        System.out.print("Masukkan Kode Barang yang ingin diubah: ");
                        String kodeBarang = scanner.nextLine().trim();
                        System.out.print("Masukkan Nama Barang Baru: ");
                        String namaBarang = scanner.nextLine().trim();
                        System.out.print("Masukkan Harga Barang Baru: ");
                        double hargaBarang = scanner.nextDouble();
                        System.out.print("Masukkan Jumlah Stok Baru: ");
                        int jumlahStok = scanner.nextInt();
                        scanner.nextLine(); // Clear buffer
                        admin.ubahBarang(kodeBarang, namaBarang, hargaBarang, jumlahStok);
                    } catch (Exception e) {
                        System.out.println("Terjadi kesalahan: " + e.getMessage());
                    }
                    break;

                case "4":
                    System.out.print("Masukkan Kode Barang yang ingin dicari: ");
                    String kodeBarang = scanner.nextLine().trim();
                    admin.cariBarang(kodeBarang);
                    break;

                case "5":
                    admin.tampilkanSemuaBarang();
                    break;

                case "6":
                    return;

                default:
                    System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        }
    }

    private static void prosesTransaksi(Scanner scanner) {
        String lanjut;
    
        // Menampilkan selamat datang
        System.out.println("\n+-----------------------------------------------------+");
        System.out.println("        Selamat Datang di Supermarket SokLaris        ");
        System.out.println("+-----------------------------------------------------+");
        // Memasukkan Date
        Date currentDate = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'pada' hh:mm:ss a zzz");
        System.out.println("Tanggal dan Waktu: " + ft.format(currentDate));
    
        do {
            try {
                System.out.print("\nMasukkan Kode Barang: ");
                String kodeBarang = scanner.nextLine().trim();
                Barang barang = admin.getBarang(kodeBarang); // Mengambil barang dari admin
    
                if (barang == null) {
                    throw new Exception("\nBarang dengan kode " + kodeBarang + " tidak ditemukan.");
                }
    
                System.out.print("Masukkan Jumlah Beli: ");
                int jumlahBeli = scanner.nextInt();
                scanner.nextLine(); // Clear buffer setelah nextInt()
    
                // Validasi jumlah beli
                if (jumlahBeli <= 0) {
                    throw new IllegalArgumentException("\nJumlah beli harus lebih dari 0.");
                }
                if (jumlahBeli > barang.getJumlahStok()) {
                    throw new IllegalArgumentException("\nJumlah beli melebihi stok yang tersedia.");
                }
    
                // Membuat nomor faktur otomatis
                String noFaktur = "FTR" + String.format("%03d", fakturCounter++);
    
                // Membuat objek Transaksi
                Transaksi transaksi = new Transaksi(noFaktur, barang.kodeBarang, barang.namaBarang, barang.hargaBarang, jumlahBeli);
                barang.kurangiStok(jumlahBeli); // Mengurangi stok barang
    
                // Menampilkan detail transaksi
                System.out.println("\n+----------------------------------------------------+");
                System.out.println(transaksi);
                System.out.println("+----------------------------------------------------+");
                System.out.println("Kasir: MHD. HASBI");
                System.out.println("+----------------------------------------------------+");
    
            } catch (Exception e) {
                System.out.println("\nTerjadi kesalahan: " + e.getMessage());
            }
    
            // Menanyakan apakah pengguna ingin melanjutkan
            System.out.print("\nApakah Anda ingin memasukkan transaksi lain? (YA/TIDAK): ");
            lanjut = scanner.next();
            scanner.nextLine(); // Clear buffer
    
        } while (lanjut.equalsIgnoreCase("ya"));
    
        System.out.println("Terima kasih telah menggunakan program ini.");
    }

    private static String generateCaptcha() {
        int length = 6;
        StringBuilder captcha = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            captcha.append(characters.charAt(index));
        }
        return captcha.toString();
    }
}