package com.smoftware.mygrocerylist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.smoftware.mygrocerylist.DbConnection.db;

/**
 * Created by steve on 12/21/16.
 */

public class GoShoppingAdapter extends BaseAdapter {

    public class ViewShoppingListHolder
    {
        public TextView TextView1;
        public TextView TextView2;
        public ImageView ImageView;
    }

    GoShoppingActivity _activity;
    Context _context;
    List<Tables.GroceryList> _groceryList;

    public GoShoppingAdapter(Context context) {
        _context = context;
        _activity = (GoShoppingActivity) context;
        _groceryList = DbConnection.db(_context).getGroceryList("SELECT * FROM GroceryList");
    }

    @Override
    public int getCount()
    {
        return _groceryList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return _groceryList.get(position).Name;
    }

    @Override
    public long getItemId(int position)
    {
        return _groceryList.get(position)._id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewShoppingListHolder viewHolder;
        if (convertView == null)
        {
            // inflate the layout
            convertView = LayoutInflater.from(_context).inflate(R.layout.list_item_main, null);

            // set up the ViewHolder
            viewHolder = new ViewShoppingListHolder();
            viewHolder.TextView1 = convertView.findViewById(R.id.mainText);
            viewHolder.TextView2 = convertView.findViewById(R.id.subText);
            viewHolder.ImageView = convertView.findViewById(R.id.Image);

            // store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewShoppingListHolder)convertView.getTag();
        }

        // get number of categories for this grocery list
        String query = String.format("SELECT COUNT (DISTINCT CatId) FROM ListCategoryGroceryItem WHERE ListId = %d", _groceryList.get(position)._id);
        int count = DbConnection.db(_context).getCount(query);
        String numItems = String.format("%d categories", count);

        int imgIcon = _context.getResources().getIdentifier(_groceryList.get(position).Icon, "mipmap", _context.getPackageName());
        viewHolder.TextView1.setText(_groceryList.get(position).Name);
        viewHolder.TextView2.setText(numItems);
        viewHolder.ImageView.setImageResource(imgIcon);
        return convertView;
    }

    public boolean CreatePDF(int position, File pdfPath)
    {
        try {
            pdfPath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fs = null;

        try {
            fs = new FileOutputStream(pdfPath, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fs == null)
            return false;

        Tables.Settings setting = DbConnection.db(_context).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.fontsize));
        float fontsize = 12;
        if (setting != null)
        {
            fontsize = Float.parseFloat(setting.Value);
        }

        Font categoryFont = FontFactory.getFont("Arial", fontsize + 3.0f, Font.BOLD, BaseColor.BLACK);
        Font groceryItemFont = FontFactory.getFont("Arial", fontsize, BaseColor.BLACK);

        // Create an instance of the document class which represents the PDF document itself.
        float margin = 45;
        Document document = new Document(PageSize.LETTER, margin, margin, margin, margin);

        // Create an instance to the PDF file by creating an instance of the PDF Writer class, using the document and the filestrem in the constructor
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, fs);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // Before we can write to the document, we need to open it.
        // Open the document to enable you to write to the document
        document.open();

        // get number of columns
        setting = DbConnection.db(_context).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.numcols));
        int numCols = 2;
        if (setting != null)
        {
            numCols = Integer.parseInt(setting.Value);
        }

        ColumnText ct = null;
        if (writer != null) {
            ct = new ColumnText(writer.getDirectContentUnder());
        }

        if (ct == null)
            return false;

        ct.setAlignment(Element.ALIGN_JUSTIFIED);
        ct.setExtraParagraphSpace(6);
        ct.setLeading(0, 1.2f);
        ct.setFollowingIndent(27);
        int column = 0;
        int status = 0;
        float colWidth = (document.left() + document.right() - (2 * margin)) / numCols;
        float[] columns1 = new float[]
        { document.left(),                    document.bottom(),    document.right(),                        document.top() };
        float[][] columns2 = new float[][] {
        { document.left(),                    document.bottom(),    document.left() + colWidth - 15,         document.top() } ,
        { document.left() + colWidth,         document.bottom(),    document.right(),                        document.top() }};
        float[][] columns3 = new float[][] {
        { document.left(),                    document.bottom(),    document.left() + colWidth - 15,         document.top() } ,
        { document.left() + colWidth,         document.bottom(),    document.left() + (colWidth * 2) - 15,   document.top() } ,
        { document.left() + (colWidth * 2),   document.bottom(),    document.right(),                        document.top() }};

        // set starting point at top of first column
        switch (numCols)
        {
            case 1:
                ct.setSimpleColumn(columns1[0], columns1[1], columns1[2], columns1[3]);
                break;
            case 2:
                ct.setSimpleColumn(columns2[column][0], columns2[column][1],
                columns2[column][2], columns2[column][3]);
                break;
            case 3:
                ct.setSimpleColumn(columns3[column][0], columns3[column][1],
                columns3[column][2], columns3[column][3]);
                break;
        }


        // srm this can be used to get embedded resource path
        //string[] sa = System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceNames();
        //foreach (string s in sa)
        //{
        //    Console.WriteLine(s);
        //}

        // get embedded checkbox image
        InputStream imgFile = null;
        try {
            imgFile = _context.getAssets().open("ic_checkbox.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imgFile == null)
            return false;

        Bitmap bmp = BitmapFactory.decodeStream(imgFile);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image img = null;
        try {
            img = Image.getInstance(stream.toByteArray());
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }

        if (img == null)
            return false;

        // get list of categories for the selected list
        String query = String.format("SELECT DISTINCT c._id, c.Name, c.Icon, c.IsSelected FROM Category c " +
                                    "INNER JOIN ListCategoryGroceryItem l ON l.CatId = c._id " +
                                    "WHERE l.ListId = %d ORDER BY c.Name", _groceryList.get(position)._id);
        List<Tables.Category> catList = DbConnection.db(_activity).getCategoryList(query);

        for (Tables.Category category : catList)
        {
            float y = ct.getYLine();

            // setup table with 2 columns for checkbox (if included) and grocery item name
            PdfPTable table = new PdfPTable(2);
            Tables.Settings include_chks = DbConnection.db(_context).getSetting(String.format("SELECT * FROM Settings WHERE Setting = \'%s\'", DbConnection.include_checkboxes));

            try {
                table.setWidthPercentage(100f);

                if (include_chks.Value.equals(DbConnection.TRUE))
                    table.setWidths(new int[] { 25, 75 });
                else
                    table.setWidths(new int[] { 5, 95 });

                table.setSpacingAfter(10f);
                table.setTotalWidth(colWidth - 15);
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            // table category name span 2 columns
            PdfPCell cellCategoryName = new PdfPCell(new Phrase(category.Name, categoryFont));
            cellCategoryName.setColspan(2);
            cellCategoryName.setBorderWidthLeft(0);
            cellCategoryName.setBorderWidthRight(0);
            cellCategoryName.setBorderWidthTop(0);
            cellCategoryName.setBorderWidthBottom(1);
            cellCategoryName.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellCategoryName.setHorizontalAlignment(Element.ALIGN_MIDDLE);
            cellCategoryName.setPaddingBottom(10);
            table.addCell(new PdfPCell(cellCategoryName));

            // add grocery items for this category
            query = String.format("SELECT DISTINCT gi._id, gi.CatId, gi.Name, gi.IsSelected, gi.Quantity FROM GroceryItem gi " +
                                "INNER JOIN ListCategoryGroceryItem l ON l.GroceryItemId = gi._id " +
                                "WHERE l.CatId = %d AND l.ListId = %d ORDER BY gi.Name", category._id, _groceryList.get(position)._id);
            List<Tables.GroceryItem> itemList = DbConnection.db(_context).getGroceryItemList(query);

            for (Tables.GroceryItem item : itemList)
            {
                PdfPCell cell = new PdfPCell();

                // checkbox
                if (include_chks.Value.equals(DbConnection.TRUE)) {
                    cell.addElement(new Chunk(img, 0, 0, true));
                    cell.setBorderWidth(0);
                    cell.setPaddingRight(0);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }
                else {
                    cell = new PdfPCell(new Phrase("", groceryItemFont));
                    cell.setBorderWidth(0);
                    cell.setPaddingLeft(0);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }

                // grocery item
                if (item.Quantity > 1) {
                    cell = new PdfPCell(new Phrase(String.format("(%d) %s", item.Quantity, item.Name), groceryItemFont));
                }
                else {
                    cell = new PdfPCell(new Phrase(item.Name, groceryItemFont));
                }

                cell.setBorderWidth(0);
                cell.setPaddingLeft(0);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            ct.addElement(table);

            // Go to next column (or page) if content doesn't fit
            if (y - CalculatePdfPTableHeight(table) < document.bottom())
            {
                if (++column == numCols)
                {
                    document.newPage();
                    column = 0;
                }

                switch (numCols)
                {
                    case 1:
                        ct.setSimpleColumn(columns1[0], columns1[1], columns1[2], columns1[3]);
                        ct.setYLine(columns1[3]);
                        break;
                    case 2:
                        ct.setSimpleColumn(columns2[column][0], columns2[column][1],
                        columns2[column][2], columns2[column][3]);
                        ct.setYLine(columns2[column][3]);
                        break;
                    case 3:
                        ct.setSimpleColumn(columns3[column][0], columns3[column][1],
                        columns3[column][2], columns3[column][3]);
                        ct.setYLine(columns3[column][3]);
                        break;
                }
            }
            else
            {
                // reset vertical position
                ct.setYLine(y);
            }

            try {
                // add content for real
                status = ct.go();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }

        // Close the document
        document.close();

        // Close the writer instance
        writer.close();

        // Always close open file handles explicitly
        try {
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public float CalculatePdfPTableHeight(PdfPTable table)
    {
        ByteArrayOutputStream ms = new ByteArrayOutputStream();

        float margin = 45;
        Document doc = new Document(PageSize.LETTER, margin, margin, margin, margin);
        PdfWriter w = null;

        try {
            w = PdfWriter.getInstance(doc, ms);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        doc.open();
        table.writeSelectedRows(0, table.getRows().size(), 0, 0, w.getDirectContent());
        doc.close();
        return table.getTotalHeight();
    }

    public void RefreshAndNotify()
    {
        _groceryList.clear();
        _groceryList = db(_context).getGroceryList("SELECT * FROM GroceryList");
        notifyDataSetChanged();
    }

    public void DeleteList(long listId)
    {
        db(_context).runQuery(String.format("DELETE FROM GroceryList WHERE _id = %d", listId));
        db(_context).runQuery(String.format("DELETE FROM ListCategory WHERE ListId = %d", listId));
        db(_context).runQuery(String.format("DELETE FROM ListCategoryGroceryItem WHERE ListId = %d", listId));
        RefreshAndNotify();
    }
}
