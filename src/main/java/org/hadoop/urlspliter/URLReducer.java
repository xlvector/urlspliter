package org.hadoop.urlspliter;

import com.google.common.io.CharStreams;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.fs.FSDataInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Iterator;

/**
 * Created by weiguo on 14-2-20.
 */
public class URLReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
    private String parent_dir_;
    private String data_base_name_;
    private long max_file_size_;
    Configuration hdfsconf_;

    @Override
    public void configure(JobConf cfg) {
        parent_dir_ = cfg.get("URL_PARENT_DIR");
        data_base_name_ = cfg.get("URL_DATA_BASE_NAME");
        max_file_size_ = Long.valueOf(cfg.get("MAX_FILE_SIZE_KB"));
        hdfsconf_ = new Configuration();
    }

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> collector, Reporter reporter) throws IOException {
        String destDIR = parent_dir_+"/"+key.toString()+"/";
        long fid = createFileID(destDIR);
        String uri = makeFileName(destDIR, fid);

        FileSystem fs = FileSystem.get(hdfsconf_);
        CompressionCodec codec = new GzipCodec();
        FSDataOutputStream outputStream = fs.create(new Path(uri));
        CompressionOutputStream out = codec.createOutputStream(outputStream);

        StringBuilder sb = new StringBuilder();
        while (values.hasNext()) {
            sb.append(values.next().toString());
            sb.append("\n");
        }
        out.write(sb.toString().getBytes("UTF-8"));
        out.close();
        fs.close();
        updateIDFile(destDIR, fid);
    }

    private long readIDFile(String destDIR) throws IOException {
        String idfile = destDIR+"id";
        FileSystem fs = FileSystem.get(URI.create(idfile), hdfsconf_);
        Path p = new Path(idfile);

        String ans="-1";
        if (fs.exists(p)) {
            FSDataInputStream in = fs.open(p);
            ans = CharStreams.toString(new InputStreamReader(in, "UTF-8"));
            in.close();
        }
        fs.close();
        return Integer.parseInt(ans.trim());
    }

    private void updateIDFile(String destDIR, long fid) throws IOException {
        String idfile = destDIR+"id";
        FileSystem fs = FileSystem.get(URI.create(idfile), hdfsconf_);
        FSDataOutputStream o = fs.create(new Path(idfile));
        o.writeChars(String.valueOf(fid));
        o.close();
        fs.close();
    }

    private String makeFileName(String destDIR, long fid) {
        return destDIR + data_base_name_ + "_"+ String.valueOf(fid) + ".gz";
    }

    private long getFileSizeKB(String filename) throws IOException {
        FileSystem fs = FileSystem.get(URI.create(filename), hdfsconf_);
        Path p = new Path(filename);
        long lenOfByte = (fs.exists(p) ? fs.getFileStatus(p).getLen() : 0);
        fs.close();
        return lenOfByte/1024;
    }

    private long createFileID(String destDIR) throws IOException {
        long lastFileID = readIDFile(destDIR);
        long nowID = (lastFileID<0) ? 0 : lastFileID;
        if (lastFileID >= 0) {
            long lastFileSize = getFileSizeKB(makeFileName(destDIR, lastFileID));
            if (lastFileSize >= max_file_size_)
                ++nowID;
        }
        return nowID;
    }
}
