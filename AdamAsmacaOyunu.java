

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class AdamAsmacaOyunu extends JFrame {

    // dosya yolları sınıf degıskenlerı 
  public static String ANA_KLASOR = "./";
    public static String RESIM_YOLU = "./Resimler/";
    public static String TXT_YOLU = "./TXTDosyalar/";

    public static String SIFRE_TXT = "./TXTDosyalar/sifre.txt";
    public static String KELIMELER_TXT = "./TXTDosyalar/kelimeler.txt";
    public static String LOG_TXT = "./TXTDosyalar/log.txt";
    public static String OYUNLAR_TXT = "./TXTDosyalar/oyunlar.txt";

    private JTabbedPane sekmeler;
    private JPanel panel1, panel2, panel3;
    private JTable tabloSkor, tabloLog;
    private DefaultTableModel modelSkor, modelLog;

    // oyun ıcı degıskenler ve butonlar
    private JPanel kelimeYeriPanel;
    private JTextField harfGirisAlani, kelimeGirisAlani;
    private JLabel resimGostermeEtiketi, sureEtiketi;
    private JButton btnHarfDene, btnKelimeDene;
    private ArrayList<JLabel> etiketListem = new ArrayList<JLabel>();

    // oyun durumu tutan elemanlar
    private String gizliKelime = "";
    private int yanlisAdimSayisi = 0;
    private int saniyeSayaci = 0;
    private Timer zamanlayici;
    private boolean oyunBittiMi = false;
    
    private int toplamTiklamaSayisi = 0; 
    private String sonDenenenHarf = "";

    

    public AdamAsmacaOyunu() {
        // ilk basta klasor var mı yok mu bakıyoruz yoksa hata verır cunku
        klasorleriVeDosyalariOlustur();

        // sifre sorma yeri projenın basında calısmalı
        boolean girisBasariliMI = sifreSorguEkrani();
        if (girisBasariliMI == false) {
            System.exit(0); // yanlıs gırerse kapat direkt
        }

        // pencere ayarları baslangıc
        setTitle("Adam Asmaca Proje Odevi - Programlama 2");
        setSize(820, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // menu ekleme kısmi
        JMenuBar jm = new JMenuBar();
        JMenu menuSecenek = new JMenu("Menu");
        JMenuItem itemYeni = new JMenuItem("Oyuna Basla / Yeniden Baslat");
        JMenuItem itemCikis = new JMenuItem("Cikis Yap");

        itemYeni.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // yeni oyunu baslatn fonksiyonu cagır
                yeniOyunKur();
            }
        });

        itemCikis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            } });
        menuSecenek.add(itemYeni);
        menuSecenek.addSeparator();
        menuSecenek.add(itemCikis);
        jm.add(menuSecenek);
        setJMenuBar(jm);

        // sekmeli yapı olusturma kısmı
        sekmeler = new JTabbedPane();

        oyunArayuzuHazırla();
        skorArayuzuHazırla();
        logArayuzuHazırla();

        sekmeler.addTab("Oyun Oynama Paneli", panel1);
        sekmeler.addTab("Eski Skorlar", panel2);
        sekmeler.addTab("Sistem Loglari", panel3);

        add(sekmeler);

        // dosyadan verileri tablolara doldur
        tablolariDoldur();

        // oyun baslasın otomatik
        yeniOyunKur();
    }

    private void klasorleriVeDosyalariOlustur() {
        try {
            File f1 = new File(RESIM_YOLU);
            File f2 = new File(TXT_YOLU);
            if (!f1.exists()) f1.mkdirs();
            if (!f2.exists()) f2.mkdirs();

            File kFile = new File(KELIMELER_TXT);
            if (kFile.exists() == false) {
                String[] kelimelerimiz = {
                    "BILGISAYAR", "MUHENDIS", "ALGORITMA", "YAZILIMCI", "VERITABANI",
                    "NESNEYE", "YONELIK", "PROGRAMLAMA", "DERLEYICI", "DONANIMLAR",
                    "INTERNET", "PROTOKOLLER", "SUNUCULAR", "ISTEMCILER", "KODLAMALAR",
                    "DEGISKENLER", "FONKSIYON", "MATRISLER", "OTOMATALAR", "MANTIKSAL",
                    "DEVRELERIM", "KULLANICILAR", "GIRISLERI", "KONTROLLERIM", "DOSYALAMALAR",
                    "TASARIMLAR", "İŞLEMCİLER", "SAYICILAR", "BELLEKLER", "ANAKARTLAR"
                };
                BufferedWriter bw = new BufferedWriter(new FileWriter(kFile));
                int i = 0;
                while(i < kelimelerimiz.length) {
                    bw.write(kelimelerimiz[i]);
                    bw.newLine();
                    i++;
                }
                bw.close();
            }

            // diger txtleride bos yaratıyorrz hata vermesın program sonradan
            new File(LOG_TXT).createNewFile();
            new File(OYUNLAR_TXT).createNewFile();

        } catch (Exception ex) {
            System.out.println("dosya olustururken sıkıntı cıktı: " + ex.getMessage());
        }
    }

    // sfre kontrol mekanızması
    private boolean sifreSorguEkrani() {
        File sifreDosyasi = new File(SIFRE_TXT);

        // sifre dosyası yoksa veya bossa yenı olustur
        if (sifreDosyasi.exists() == false || sifreDosyasi.length() == 0) {
            String ilkSifre = "";
            while (ilkSifre == null || ilkSifre.trim().equals("")) {
                ilkSifre = JOptionPane.showInputDialog(null, "Sistemde kayitli sifre yok yeni bir giris sifresi girin:", "Sifre kaydetme", JOptionPane.INFORMATION_MESSAGE);
                if (ilkSifre == null) {
                    return false;
                }
            }
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(sifreDosyasi));
                bw.write(ilkSifre.trim());
                bw.close();
                JOptionPane.showMessageDialog(null, "Sifreniz basariyla kaydedildi.");
                logYazdir("Sifre ilk kez belirlendi.");
            } catch (Exception e) {
                System.out.println("sifre yazma hatası");
                return false;
            }
        }

        // kayıtlı olan sıfreyı dosyadan okuma kısmı
        String dosyadakiSifre = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(sifreDosyasi));
            dosyadakiSifre = br.readLine();
            br.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "şifre okunamadi");
            return false;
        }

        // 3 defa denneme hakkı var 
        int hakSayaci = 3;
        while (hakSayaci > 0) {
            String girilen = JOptionPane.showInputDialog(null, "Giris sifresini yaziniz (kalann hak: " + hakSayaci + "):", "Sifre kontrolü", JOptionPane.QUESTION_MESSAGE);
            
            if (girilen == null) {
                logYazdir("Kullanici giris yapmaktan vazgecti.");
                return false;
            }

            if (girilen.equals(dosyadakiSifre)) {
                logYazdir("Sisteme basarili sekilde girs yapildi.");
                return true;
            } else {
                hakSayaci--;
                logYazdir("Hatali sifre girildi. Kalan hak: " + hakSayaci);
                JOptionPane.showMessageDialog(null, "Yanlis sifre girdiniz", "hata", JOptionPane.ERROR_MESSAGE); }
        }

        JOptionPane.showMessageDialog(null, "3 kere yanlis girdiniz program kapaniyor.", "Giris engellendi", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // txt ye zamanlı kayıt atma
    private void logYazdir(String logEtiketi) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_TXT, true));
            String simdikiZaman = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            bw.write("[" + simdikiZaman + "] -> " + logEtiketi);
            bw.newLine();
            bw.close();
        } catch (Exception ex) {
            // log yazamazsa konsola bassın
            System.out.println("log yazma hatası olustu");
        }
    }

    // oyun oynama panel tasarımı 
    private void oyunArayuzuHazırla() {
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(5, 5));
        // ust tarafa sure yerlestirme
        JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sureEtiketi = new JLabel("Gecen Sure: 0 saniye");
        sureEtiketi.setFont(new Font("Arial", Font.BOLD, 14));
        ustPanel.add(sureEtiketi);
        panel1.add(ustPanel, BorderLayout.NORTH);

        // sol orta taraftaki kelime harfleri ve girdi kutuları
        JPanel ortaSolPanel = new JPanel();
        ortaSolPanel.setLayout(new BoxLayout(ortaSolPanel, BoxLayout.Y_AXIS));

        kelimeYeriPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        kelimeYeriPanel.setBorder(BorderFactory.createTitledBorder("Tahmin Edilecek Kelime"));
        JPanel girdilerPaneli = new JPanel(new GridLayout(2, 3, 5, 5));
        girdilerPaneli.setBorder(BorderFactory.createTitledBorder("Giris Yapin"));

        girdilerPaneli.add(new JLabel("Harf Girisi:"));
        harfGirisAlani = new JTextField();
        btnHarfDene = new JButton("Harfi Dene");
        girdilerPaneli.add(harfGirisAlani);
        girdilerPaneli.add(btnHarfDene);

        girdilerPaneli.add(new JLabel("Kelime komple Tahmin:"));
        kelimeGirisAlani = new JTextField();
        btnKelimeDene = new JButton("Kelimeyi dene");
        girdilerPaneli.add(kelimeGirisAlani);
        girdilerPaneli.add(btnKelimeDene);
        ortaSolPanel.add(kelimeYeriPanel);
        ortaSolPanel.add(girdilerPaneli);
        panel1.add(ortaSolPanel, BorderLayout.CENTER);

        // sag taraf resim kutusu kural
        JPanel sagPanel = new JPanel(new BorderLayout());
        sagPanel.setBorder(BorderFactory.createTitledBorder("Asilma durumu (11 Hak)"));
        resimGostermeEtiketi = new JLabel();
        resimGostermeEtiketi.setPreferredSize(new Dimension(240, 280));
        resimGostermeEtiketi.setHorizontalAlignment(JLabel.CENTER);
        sagPanel.add(resimGostermeEtiketi, BorderLayout.CENTER);
        panel1.add(sagPanel, BorderLayout.EAST);
// butonların dinleyicileri listener ekleme
        btnHarfDene.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toplamTiklamaSayisi++; 
                harfKontrolEt(); }
        });
        btnKelimeDene.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toplamTiklamaSayisi++;
                kelimeKontrolEt();
            }
        });

        // saniyelik zaman sayacı kural
        zamanlayici = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (oyunBittiMi == false) {
                    saniyeSayaci++;
            sureEtiketi.setText("Gecen sure: " + saniyeSayaci + " saniye");
                }
            }
        });
    }

    // eski skorları listeleme paneli kuralı
    private void skorArayuzuHazırla() {
        panel2 = new JPanel(new BorderLayout(5, 5));
        String[] basliklar = {"oynanma zamanı", "sure (sn)", "Sonuc durumu"};
        modelSkor = new DefaultTableModel(basliklar, 0);
        tabloSkor = new JTable(modelSkor);
 JScrollPane sp = new JScrollPane(tabloSkor);
        panel2.add(sp, BorderLayout.CENTER);

        JButton btnTemizleS = new JButton("Skorlari Temizle");
        panel2.add(btnTemizleS, BorderLayout.SOUTH);

        btnTemizleS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // sifre sorup dosyayı siliyoruz
                dosyaTemizleIslemi(OYUNLAR_TXT, modelSkor);
            } });
    }

    // logları listeleme paneli
    private void logArayuzuHazırla() {
        panel3 = new JPanel(new BorderLayout(5, 5));
        String[] basliklar = {"Log Detay Satirlari"};
        modelLog = new DefaultTableModel(basliklar, 0);
        tabloLog = new JTable(modelLog);
        JScrollPane sp = new JScrollPane(tabloLog);
        panel3.add(sp, BorderLayout.CENTER);
        JButton btnTemizleL = new JButton("Log kayitlarini Temizle");
        panel3.add(btnTemizleL, BorderLayout.SOUTH);

        btnTemizleL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
         // sifre sorup dosyayı siliyoruz 
                dosyaTemizleIslemi(LOG_TXT, modelLog);
            }
        });
    }

    // dosyadan kelime secip oyunu sıfırlayan fonksiyon
             private void yeniOyunKur() {
        ArrayList<String> geciciKelimeListesi = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(KELIMELER_TXT));
            String okunanSatir;
            while ((okunanSatir = br.readLine()) != null) {
                if (okunanSatir.trim().length() >= 6) {
                    geciciKelimeListesi.add(okunanSatir.trim().toUpperCase());
       }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("kelimeler okunurken sıkıntı oldu");
            return;}
        if (geciciKelimeListesi.size() == 0) {
            System.out.println("kelimeler listesi bos kaldi ");
            return;
        }

        Random r = new Random();
        int rastgeleIndex = r.nextInt(geciciKelimeListesi.size());
        gizliKelime = geciciKelimeListesi.get(rastgeleIndex);

        // degerleri sıfırlayalım
        yanlisAdimSayisi = 0;
        saniyeSayaci = 0;
        oyunBittiMi = false;
        sureEtiketi.setText("Gecen Sure: 0 saniye");
        resmiArayuzeBas();
        kelimeYeriPanel.removeAll();
        etiketListem.clear();
        int k = 0;
        while(k < gizliKelime.length()) {
            JLabel lab = new JLabel("*");
            lab.setFont(new Font("Courier New", Font.BOLD, 24));
            lab.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.RED));
            kelimeYeriPanel.add(lab);
            etiketListem.add(lab);
            k++;}
        // arayüz yenilensin gorunsun labeller
        kelimeYeriPanel.revalidate();
        kelimeYeriPanel.repaint();

        // sayacı baslat sıfırdan
        zamanlayici.stop();
        zamanlayici.start();

        btnHarfDene.setEnabled(true);
        btnKelimeDene.setEnabled(true);
    }

    // resimleri ekrana basma fonksiyonu 
    private void resmiArayuzeBas() {
        // resim isimleri 1.jpg, 2.jpg diye gidiyor kural geregi
        String resminTamAdi = (yanlisAdimSayisi + 1) + ".jpg";
        File imgFile = new File(RESIM_YOLU + resminTamAdi);
        if (imgFile.exists()) {
            ImageIcon imgIcon = new ImageIcon(imgFile.getAbsolutePath());
                // resmi olceklendirme yapıyoruz sığsın diye ekrana
            Image geciciResim = imgIcon.getImage().getScaledInstance(220, 260, Image.SCALE_SMOOTH);
            resimGostermeEtiketi.setIcon(new ImageIcon(geciciResim));
        } else {
            resimGostermeEtiketi.setIcon(null);
            resimGostermeEtiketi.setText("Resim bulunamadi: " + resminTamAdi);
        }
    }
    // harf kontrol logic kodları
    private void harfKontrolEt() {
        String inputHarf = harfGirisAlani.getText().trim().toUpperCase();
        harfGirisAlani.setText("");

        if (inputHarf.length() != 1 || inputHarf.equals("")) {
            JOptionPane.showMessageDialog(this, "Tek bir tane harf yazmalisiniz!");
            return; }
        sonDenenenHarf = inputHarf;
        char arananChar = inputHarf.charAt(0);
        boolean kelimedeVarMi = false;

        // donguyle arıyoruz kelimede var mı yok mu
        for (int i = 0; i < gizliKelime.length(); i++) {
            if (gizliKelime.charAt(i) == arananChar) {
                etiketListem.get(i).setText(String.valueOf(arananChar));
                kelimedeVarMi = true;   }
        }

        // yoksa yanlisi artir resmi degistir
        if (kelimedeVarMi == false) {
            yanlisAdimSayisi++;
            resmiArayuzeBas();
            // 11 hak kontrolü
            if (yanlisAdimSayisi >= 11) {
                macBitti(false);
            }
        } else {
            // kazandı mı kontrol et
            boolean hepsiAcildiMi = true;
            for (int m = 0; m < etiketListem.size(); m++) {
                if (etiketListem.get(m).getText().equals("*")) {
                    hepsiAcildiMi = false;
        }
            }
            if (hepsiAcildiMi == true) {
                macBitti(true);
            }
        }
    }
    // kelime kontrol logic mekanızması
    private void kelimeKontrolEt() {
        String inputKelime = kelimeGirisAlani.getText().trim().toUpperCase();
        kelimeGirisAlani.setText("");

        if (inputKelime.equals("")) {
            JOptionPane.showMessageDialog(this, "Kelime alanini bos birakamazsiniz.");
            return;
        }
        // esitse direk biter ve kazanır
        if (inputKelime.equals(gizliKelime)) {
            int x = 0;
            while(x < gizliKelime.length()) {
                etiketListem.get(x).setText(String.valueOf(gizliKelime.charAt(x)));
                x++;
            }
            macBitti(true);
        } else {
            yanlisAdimSayisi++;
            resmiArayuzeBas();
            if (yanlisAdimSayisi >= 11) {
                macBitti(false);
   }
        }
    }

    // oyun bittiginde txt dosyasına kayıt atma
    private void macBitti(boolean durumKazanma) {
        oyunBittiMi = true;
        zamanlayici.stop();

        btnHarfDene.setEnabled(false);
        btnKelimeDene.setEnabled(false);
        String sonucText = "";
        if(durumKazanma == true) {
            sonucText = "KAZANDI";
            JOptionPane.showMessageDialog(this, "Tebrikler bildinizkelime: " + gizliKelime);
        } else {
            sonucText = "KAYBETTI";
            JOptionPane.showMessageDialog(this, "Maalesef kaybettiniz kelime suydu: " + gizliKelime);
        }

        String tarihFormatimiz = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // oyunlar.txt ye yazma islemi kural geregi noktalı virgul ile ayırdım split etmesi kolay olsun dıye
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(OYUNLAR_TXT, true));
            bw.write(tarihFormatimiz + ";" + saniyeSayaci + ";" + sonucText);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            System.out.println("skor kayit edilemedi.");
        }
        // tabloları hemen guncelle ekranda gorunsun
        tablolariDoldur();
        System.exit(0);
    }

    // dosyalardaki verileri satır satır okuyup jtable lara yukleme 
    private void tablolariDoldur() {
        // skor tablosu yukleme
        modelSkor.setRowCount(0);
        try {
            BufferedReader br = new BufferedReader(new FileReader(OYUNLAR_TXT));
            String veriSatiri;
            while ((veriSatiri = br.readLine()) != null) {
                String[] parcalanmisVeri = veriSatiri.split(";");
                if (parcalanmisVeri.length == 3) {
                    modelSkor.addRow(parcalanmisVeri);         }
            }
            br.close();
        } catch (Exception ex) {
            System.out.println("skor dosyası yuklenırken hata");
        }

        // log tablosu yukleme
        modelLog.setRowCount(0);
        try {
            BufferedReader br = new BufferedReader(new FileReader(LOG_TXT));
            String veriSatiri;
         while ((veriSatiri = br.readLine()) != null) {
                modelLog.addRow(new Object[]{veriSatiri});
            }
            br.close();
        } catch (Exception ex) {
            System.out.println("log dosyası yuklenırken hata");
        }
    }

    // sifre korumalı dosya temizleme butonu kodu
    private void dosyaTemizleIslemi(String hangiDosyaYolu, DefaultTableModel hangiModel) {
        String sistemdekiSifre = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(SIFRE_TXT));
      sistemdekiSifre = br.readLine();
            br.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Sifre dosyasi okunamiyor.");
            return;
        }

        String girilenSifre = JOptionPane.showInputDialog(this, "verileri kalici silmek icin giris sifresini girin", "guvenlik onayi", JOptionPane.WARNING_MESSAGE);
        
        if (girilenSifre == null) {
      return; // vazgecti demek
         }

        if (girilenSifre.equals(sistemdekiSifre)) {
            try {
                // dosyayı bosaltma kodu printwriter ile uzerine yazınca bosalıyor
                PrintWriter pw = new PrintWriter(hangiDosyaYolu);
                pw.print("");
                pw.close();
                
                hangiModel.setRowCount(0); // tabloyu da sıfırla
                logYazdir(hangiDosyaYolu + " dosyasi icerigi sifirlandi.");
                JOptionPane.showMessageDialog(this, "Icerik basariyla temizlendi.");
                
                tablolariDoldur(); // tabloları yenıle
            } catch (Exception e) {
                System.out.println("temizlerken hata");
            }
        } else {
            logYazdir("Yetkisiz kisi verileri silmeye calisti yanlis sifre");
            JOptionPane.showMessageDialog(this, "Sifre yanlis veriler silinmedi ", "yetkisiz Islem", JOptionPane.ERROR_MESSAGE);
        }
    }

    // main metodu uygulamanın giris noktası
    public static void main(String[] args) {
        // swing thread guvenlıgı ıcın cagırıyoruz
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AdamAsmacaOyunu().setVisible(true);
            }
        });
    }
}
