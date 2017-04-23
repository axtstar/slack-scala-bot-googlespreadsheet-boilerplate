package com.axtstar.slackScalaSample

import java.io.{FileInputStream, InputStreamReader}
import java.util
import java.util.Collections

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.{FileContent, HttpTransport}
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.{Drive, DriveScopes}
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.google.api.services.sheets.v4.model.ValueRange

import collection.JavaConversions._


object GoogleUtils {
  def apply = new GoogleUtils
}

class GoogleUtils {
  /** Application name. */
  def APPLICATION_NAME = "Google API"

  /** Directory to store user credentials for this application. */
  def DATA_STORE_DIR = new java.io.File(
    System.getProperty("user.home"), ".credentials/.store")

  /** Global instance of the {@link FileDataStoreFactory}. */
  var DATA_STORE_FACTORY: FileDataStoreFactory = _

  /** Global instance of the JSON factory. */
  def JSON_FACTORY = JacksonFactory.getDefaultInstance()

  /** Global instance of the HTTP transport. */
  var HTTP_TRANSPORT: HttpTransport = _

  /** Global instance of the scopes required by this quickstart.
    *
    * If modifying these scopes, delete your previously saved credentials
    * at ~/.credentials/drive-java-quickstart
    */
  private def SCOPES = List(DriveScopes.DRIVE_FILE, SheetsScopes.DRIVE, SheetsScopes.SPREADSHEETS)

  def Init = {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR)
    } catch {
      case t: Throwable =>
        t.printStackTrace();
        System.exit(1);
    }
  }

  private def authorize(seacretJson: String) = {

    // Load client secrets.
    val in = new FileInputStream(seacretJson)
    val clientSecrets =
      GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))

    // Build flow and trigger user authorization request.
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(DATA_STORE_FACTORY)
      .setAccessType("offline")
      .build()
    val credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
    credential
  }

  private def getDriveService(seacretJson: String): Drive = {
    val credential = authorize(seacretJson)

    new Drive.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(APPLICATION_NAME)
      .build()
  }

  private def getSeetsService(seacretJson: String): Sheets = {
    val credential = authorize(seacretJson)

    new Sheets.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(APPLICATION_NAME)
      .build()
  }


  /** GoogleDriveに指定した名前でファイルをアップロードする
    *   @param seacretJson クレデンシャル
    *   @param folderID アップロードするフォルダ
    *   @param filePath アップロードするファイル
    *   @param setName 名前
    * */
  def upload(seacretJson: String, folderID: String, filePath: java.io.File, setName: String) = {
    Init
    // Build a new authorized API client service.
    val service = getDriveService(seacretJson)

    val mediaContent = new FileContent("text/csv", filePath)
    val fileMetadata = new File()
    var isFolder = false
    if (folderID != null && folderID != "") {
      isFolder = true
      fileMetadata.setParents(Collections.singletonList(folderID))
    }
    fileMetadata.setName(setName)
    fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet")

    service.files().create(fileMetadata, mediaContent)
      .setFields(if (isFolder) {
        "id, parents"
      } else {
        "id"
      })
      .execute()
  }

  def list(seacretJson: String, count: Integer) = {
    Init
    // Build a new authorized API client service.
    val service = getDriveService(seacretJson)

    val result = service.files().list()
      .setPageSize(count)
      .setFields("nextPageToken, files(id, name)")
      .execute()

    val files = result.getFiles()
    files.map { file =>
      s"${file.getName()} ${file.getId()}"
    }
  }

  /** Googleスプレッドシートにvaluesをセットする
    *   @param seacretJson クレデンシャル
    *   @param spreadsheetId　スプレッドシートID：URLのところ
    *   @param range レンジ A:B2みたいな形式
    *   @param values 張り付ける配列
    * */
  def setValueToSheet(seacretJson: String, spreadsheetId: String, range: String, values:util.List[util.List[Object]]) = {
    Init
    // Build a new authorized API client service.
    val service = getSeetsService(seacretJson)

    val response = service.spreadsheets().values()
      .get(spreadsheetId, range)
      .execute()

    val r:ValueRange = new ValueRange().setValues(values).setMajorDimension("ROWS")

    val values2 = service.spreadsheets().values()
      .update(spreadsheetId, range,  r)
      .setValueInputOption("RAW")
      .execute()
  }

  /** Googleスプレッドシートからvaluesを取得する
    *   @param seacretJson クレデンシャル
    *   @param spreadsheetId　スプレッドシートID：URLのところ
    *   @param range レンジ A:B2みたいな形式
    * */
  def getValueToSheet(seacretJson: String, spreadsheetId: String, range: String):util.List[util.List[Object]] = {
    Init
    // Build a new authorized API client service.
    val service = getSeetsService(seacretJson)

    val response = service.spreadsheets().values()
      .get(spreadsheetId, range)
      .execute()

    response.getValues

  }

}
