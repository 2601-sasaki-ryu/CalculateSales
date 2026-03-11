package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	//追加したエラーメッセージ
	private static final String FILE_NOT_SEQUENCE = "売上ファイル名が連番になっていません";
	private static final String AMOUNT_OVER = "合計金額が10桁を超えました";
	//<該当ファイル名>が固定ではないため、一文で定義できなかった。
	private static final String CODE_INVALID_NUMBER ="の支店コードが不正です";
	private static final String INVALID_FORMAT = "のフォーマットが不正です";
	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		//コマンドライン引数の確認、エラー処理
		if (args.length != 1) {
			//コマンドライン引数が1つ設定されていなかった場合は、
			//エラーメッセージをコンソールに表示します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

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
		for(int i = 0; i < files.length; i++) {

			if(!files[i].isFile() && files[i].getName().matches("^[0-9]{8}.rcd$")) {
				//売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
				rcdFiles.add(files[i]);
			}

		}

		//.rcdファイルが連番になっていない場合終了したい。
		//比較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ小さい数
		Collections.sort(rcdFiles);
		for(int i = 0; i < rcdFiles.size() - 1; i++) {
			//latter - former = 1になれば連番、.get(i)呼び出し順で後者は.get(i) + 1、File型だとエラーになるので.getNameでStringにした
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を比較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表示します。
				System.out.println(FILE_NOT_SEQUENCE);
				return;
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
				//売上ファイルの中身は新しいListを作成して保持
				List<String> saleContents = new ArrayList<>();
				String line;
				while((line = br.readLine()) != null) {
					//中身の保持のためString型
					saleContents.add(line);
				}
				//売上ファイルが2行になっているか確認、エラー処理
				if(saleContents.size() != 2) {
				    //売上ファイルの行数が2行ではなかった場合は、
				    //エラーメッセージをコンソールに表示します。
					System.out.println(rcdFiles.get(i).getName() + INVALID_FORMAT);
					return;
				}

				//1行ずつListに追加されている(支店コード、金額）
				//拡張性を考慮した時、後から.get()を増やせば良いと考えた
				String branchCode = saleContents.get(0);
				String saleLine = saleContents.get(1);

				//支店コードに該当がなかった場合エラー処理したい
				if (!branchNames.containsKey(branchCode)) {
					//支店情報を保持しているMapに売上ファイルの支店コードが存在しなかった場合は、
					//エラーメッセージをコンソールに表示します。
					System.out.println(rcdFiles.get(i).getName() + CODE_INVALID_NUMBER);
					return ;
				}

				if(!saleLine.matches("^[0-9]*$")) {
					//売上金額が数字ではなかった場合は、
					//エラーメッセージをコンソールに表示
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//※詳細は後述で説明
				long fileSale = Long.parseLong(saleLine);

				//読み込んだ売上⾦額を加算します。
				//※詳細は後述で説明
				//Long saleAmount = 売上金額を入れたMap.get(支店コード) + long に変換した売上金額;
				Long saleAmount = branchSales.get(branchCode) + fileSale;

				//10桁超えた場合にエラー処理
				if(saleAmount >= 10000000000L){
					System.out.println(AMOUNT_OVER);
				}

				//加算した売上金額をMapに追加します。
				branchSales.put(branchCode, saleAmount);

			} catch (IOException e) {
				// 例外処理（エラーが発生した場合の対応）
				System.out.println(UNKNOWN_ERROR);
				return;

			} finally {
				// ファイルを閉じます。
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
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

			//ファイルの存在を確認したい
			if(!file.exists()) {
				//支店定義ファイルが存在しない場合、コンソールにエラーメッセージを表示します。
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {

				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//書き込み
				String[] items = line.split(",");

				//支店定義ファイルのフォーマットが不正な場合は、処理を終了したい
				//^:直後に始まる$：直前のパターンで終了する
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
					//支店定義ファイルの仕様が満たされていない場合、
					//エラーメッセージをコンソールに表示します。
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

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
		BufferedWriter bw = null;

		try {
			//ここで試行錯誤中
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			//中身がなくなるまで1つずつ取り出す
			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for文で繰り返されているので、1つ目のキーが取得できたら、
				//2つ目の取得...といったように、次々とkeyという変数に上書きされていきます。

				//line = key(支店コード)、支店名、加算した売上金額
				String line = key + "," + branchNames.get(key) + "," + branchSales.get(key);

				// .writeでファイルに書き込む、newLine()で改行
				bw.write(line);
				bw.newLine();
			}

		} catch (IOException e) {
			// 書き込み中にエラーが起きた場合
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				// ファイルを閉じる
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
