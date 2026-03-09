package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles(); 
		
		//先にファイルの情報を格納する List(ArrayList) 宣言します。
		List<File> rcdFiles = new ArrayList<>(); 
		//ここですべてのファイルから数字8桁のrcdファイルを取り出したい
		for(int i = 0; i < files.length ; i++) { 
				if(files[i].getName().matches("^[0-9]{8}.rcd$")) { 
			            //売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
					rcdFiles.add(files[i]); 		
				} 
		}
		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for(int i = 0; i < rcdFiles.size(); i++) {

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			BufferedReader br = null;
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);
			
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています→.readLineは1行ずつ読み出し、2回で1行目と2行目で出力したい
				String branchCode = br.readLine(); 
				String saleLine = br.readLine();
			//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
			//※詳細は後述で説明
			long fileSale = Long.parseLong(saleLine); 
			
			//読み込んだ売上⾦額を加算します。
			//※詳細は後述で説明
			//Long saleAmount = 売上金額を入れたMap.get(支店コード) + long に変換した売上金額;
			Long saleAmount = branchSales.get(branchCode)+ fileSale;

			//加算した売上金額をMapに追加します。
			branchSales.put(branchCode, saleAmount);
			  } catch (IOException e) {
			        // 例外処理（エラーが発生した場合の対応）
			        System.out.println("UNKNOWN_ERROR");
			        return; 
			    } finally {
			        // ファイルを閉じます。
			        if (br != null) {
			            try {
			                br.close();
			            } catch (IOException e) {
			                System.out.println("UNKNOWN_ERROR");
			            }
			        }
			    }  
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//書き込み
			    String[] items = line.split(",");
			    //Mapに追加する2つの情報をputの引数として指定します。
                //支店定義フォルダの支店コード、支店名の文字列分割、保持
          
			    branchNames.put(items[0], items[1]);
			    //支店コードと売上金額を保持する

			    branchSales.put(items[0], 0L);
				System.out.println(line);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		return true;
	}

}
