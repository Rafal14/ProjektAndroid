package lab.projekt.aplikacja;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class GpsDBHelper extends SQLiteOpenHelper {
    //nazwa bazy danych
    private static final String DATABASE_NAME = "location.db";

    //wersja bazy danych
    private static final int DATABASE_VESION = 1;

    //nazwa tabeli w bazie danych
    private static final String COORDINATES_TABLE_NAME = "COORDINATES";

    //atrybut klucza podstawowego tabeli współrzednych
    private static final String COLUMN_ID_COORDINATES = "ID_COORDINATES";

    //atrybut określający numer trasy
    private static final String COLUMN_TRACK_NUM = "TRACK_NUMBER";

    //atrybut określający szerokość geograficzną
    private static final String COLUMN_LATITUDE = "LATITUDE";

    //atrybut określający długość geograficzną
    private static final String COLUMN_LONGITUDE = "LONGITUDE";

    //nazwa tabeli w bazie danych
    private static final String STATISTICS_TABLE_NAME = "STATISTICS";

    //atrybut klucza podstawowego tabeli współrzednych
    private static final String COLUMN_ID_STATISTICS = "ID_STATISTICS";

    //atrybut określający czas treningu
    private static final String COLUMN_TIME = "TIME";

    //atrybut określający pokonany dystans
    private static final String COLUMN_DISTANCE = "DISTANCE";

    //do tworzenia tabeli bazy danych zawierającej współrzędne geograficzne pobrane z GPS
    private static final String TABLE_CREATE_COORDINATES = "CREATE TABLE IF NOT EXISTS "
            + COORDINATES_TABLE_NAME + " (" + COLUMN_ID_COORDINATES
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TRACK_NUM + " INTEGER, "
            + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";

    //do tworzenia  tabeli danych zawierającej współrzędne geograficzne pobrane z GPS
    private static final String TABLE_CREATE_STATISTICS = "CREATE TABLE IF NOT EXISTS "
            + STATISTICS_TABLE_NAME + " (" + COLUMN_ID_STATISTICS
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TRACK_NUM + " INTEGER, "
            + COLUMN_TIME + " REAL, " + COLUMN_DISTANCE + " REAL)";

    private static final String DELETE_TABLE_COORDINATES = "DROP TABLE IF EXISTS COORDINATES";
    private static final String DELETE_TABLE_STATISTICS = "DROP TABLE IF EXISTS STATISTICS";


    /**
     * Konstruktor obiektu klasy GpsDBHelper
     * @param context
     */
    public GpsDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VESION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //tworzenie tabel w bazie danych
        db.execSQL(TABLE_CREATE_COORDINATES);
        db.execSQL(TABLE_CREATE_STATISTICS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE_COORDINATES);
        db.execSQL(DELETE_TABLE_STATISTICS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    /**
     * Wstawia dane do tabeli coordianes
     * @param trackNumber określa numer trasy
     * @param latitude określa wartość szerokości geograficznej
     * @param longitude określa wartość długości geograficznej
     * @return true jeśli powiodło się wstawnianie danych do tabli, false w przeciwnym przypadku
     */
    public boolean insertIntoCoordinates (int trackNumber, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("track_number", trackNumber);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        db.insert("coordinates", null, values);
        return true;
    }

    /**
     * Wstawia dane do tabeli statistics
     * @param trackNumber
     * @param time
     * @param distance
     * @return true jeśli powiodło się wstawnianie danych do tabli, false w przeciwnym przypadku
     */
    public boolean insertIntoStatistics (int trackNumber, double time, double distance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("track_number", trackNumber);
        values.put("time", time);
        values.put("distance", distance);
        db.insert("statistics", null, values);
        return true;
    }

    /**
     * Pobranie danych z tabli coordiantes na podstawie określonej warotści numeru ścieżli
     * @param trackNumber określa numer trasy
     * @return kursor na zwracane dane
     */
    public Cursor getTrackData(int trackNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from coordinates where track_number = ?",
                new String[]{Integer.toString(trackNumber)});
    }

    /**
     * Pobiera wszystkie dane z tabeli coordiantes
     * @return kursor na dane z tabeli coordiantes
     */
    public Cursor getAllTrackData() {
        String[] columns = {COLUMN_ID_COORDINATES, COLUMN_TRACK_NUM, COLUMN_LATITUDE,
                COLUMN_LONGITUDE};
        SQLiteDatabase db = this.getReadableDatabase();
            return db.query(COORDINATES_TABLE_NAME, columns, null, null,
                null, null, null);
    }

    /**
     * Wyznacza największą wartość numeru ścieżki znajdującego się w tabli coordiantes
     * @return nawiększa wartość numeru ścieżki z tabli coordiantes
     */
    public int getMaxTrackData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(COORDINATES_TABLE_NAME, new String [] {"MAX(TRACK_NUMBER)"},
                null, null, null, null, null);
        cur.moveToFirst();
        if (cur.getType(0) == Cursor.FIELD_TYPE_NULL) {
            return 0;
        }
        int val = cur.getInt(0);
        cur.close();
        return val;
    }


    /**
     * Pobranie czasu i dystansu dla ścieżki o określonym numerze
     * @param trackNumber określa numer trasy
     * @return kursor na zwracane dane
     */
    public Cursor getStatistic(int trackNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from statistics where track_number = ?",
                new String[]{Integer.toString(trackNumber)});
    }

    /**
     * Pobiera wszystkie dane z tabeli statistics (id, numer trasy, czas, dystans)
     * @return kursor na dane z tabeli statistics
     */
    public Cursor getAllStatistic() {
        String[] columns = {COLUMN_ID_STATISTICS, COLUMN_TRACK_NUM, COLUMN_TIME, COLUMN_DISTANCE};
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(STATISTICS_TABLE_NAME, columns, null, null,
                null, null, null);
    }

    /**
     * Usuwa wszyskie współrzędne dla zadanej wartości ścieżki
     * @param trackNumber określa numer trasy
     * @return liczba usuniętych wierszy
     */
    public Integer deleteTrack (int trackNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("coordinates", "track_number = ? ",
                new String[] { Integer.toString(trackNumber) });
    }

    /**
     * Usuwa dane o czasie i dystansie dla zadanej wartości ścieżki
     * @param trackNumber określa numer trasy
     * @return liczba usuniętych wierszy
     */
    public Integer deleteStatistic (int trackNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("statistics", "track_number = ? ",
                new String[] { Integer.toString(trackNumber) });
    }

    /**
     * Pobiera liczbę wierszy w tabeli współrzędnych
     * @return liczba wierszy w tabeli coordinates
     */
    public int getCoordiantesRowsNumber(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, COORDINATES_TABLE_NAME);
    }

    /**
     * Pobiera liczbę wierszy w tabeli statistics
     * @return liczba wierszy w tabeli statistics
     */
    public int getStatRowsNumber(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, STATISTICS_TABLE_NAME);
    }

    /**
     * Aktualizuje dane w tabeli statistics
     * @param trackNumber określa numer ścieżki
     * @param time określa czas pokonania trasy
     * @param distance okreśła długość trasy
     * @return wartość logiczna w zależności od pomyślości wykonania aktualizacji danych w tabeli
     */
    public boolean updateStatistics (int trackNumber, double time, double distance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("distance", distance);
        db.update("coordinates", values, "track_number = ? ",
                new String[] { Integer.toString(trackNumber) } );
        return true;
    }
}
