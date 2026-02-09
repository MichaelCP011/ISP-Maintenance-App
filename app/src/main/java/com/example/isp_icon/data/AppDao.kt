package com.example.isp_icon.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppDao {
    // --- BAGIAN LOKASI ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLokasi(list: List<LokasiEntity>)

    @Query("SELECT * FROM tabel_lokasi ORDER BY namaSite ASC")
    suspend fun getAllLokasi(): List<LokasiEntity>

    // --- BAGIAN PERSONIL ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonil(list: List<PersonilEntity>)

    @Query("SELECT * FROM tabel_personil ORDER BY namaLengkap ASC")
    suspend fun getAllPersonil(): List<PersonilEntity>

    // --- BAGIAN PERTANYAAN ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPertanyaan(list: List<PertanyaanEntity>)

    // Ambil pertanyaan berdasarkan kategori (misal: "Genset")
    @Query("SELECT * FROM tabel_pertanyaan WHERE kategori = :kategoriTarget")
    suspend fun getPertanyaanByKategori(kategoriTarget: String): List<PertanyaanEntity>

    // Ambil semua kategori unik (untuk menu ikon)
    @Query("SELECT DISTINCT kategori FROM tabel_pertanyaan")
    suspend fun getAllKategori(): List<String>

    // Hapus semua data (opsional, untuk reset)
    @Query("DELETE FROM tabel_pertanyaan")
    suspend fun clearPertanyaan()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeaderOptions(list: List<HeaderOptionEntity>)

    @Query("DELETE FROM tabel_opsi_header")
    suspend fun clearHeaderOptions()

    // Ambil data berdasarkan kategori (misal: ambil semua 'nama_site')
    @Query("SELECT nilai FROM tabel_opsi_header WHERE kategori = :kategoriTarget ORDER BY nilai ASC")
    suspend fun getOptionsByCategory(kategoriTarget: String): List<String>
}