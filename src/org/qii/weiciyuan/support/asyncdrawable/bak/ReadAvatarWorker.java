//package org.qii.weiciyuan.support.asyncdrawable;
//
//import android.graphics.Bitmap;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.Handler;
//import android.util.DisplayMetrics;
//import android.util.LruCache;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import org.qii.weiciyuan.R;
//import org.qii.weiciyuan.support.debug.DebugColor;
//import org.qii.weiciyuan.support.error.WeiboException;
//import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
//import org.qii.weiciyuan.support.file.FileLocationMethod;
//import org.qii.weiciyuan.support.file.FileManager;
//import org.qii.weiciyuan.support.imagetool.ImageTool;
//import org.qii.weiciyuan.support.lib.MyAsyncTask;
//import org.qii.weiciyuan.support.utils.GlobalContext;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * User: qii
// * Date: 13-2-9
// */
//public class ReadAvatarWorker extends MyAsyncTask<String, Void, Bitmap> implements IPictureWorker {
//
//
//    private LruCache<String, Bitmap> lruCache;
//    private String data = "";
//    private final List<WeakReference<ImageView>> viewList = new ArrayList<WeakReference<ImageView>>();
//    private Map<String, PictureBitmapWorkerTask> taskMap;
//    private GlobalContext globalContext;
//    private List<FileDownloaderHttpHelper.DownloadListener> downloadListenerList = new CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener>();
//
//    private FileLocationMethod method;
//
//    private WeiboException failedBecauseOfNetwork;
//    private Handler handler;
//
//    public String getUrl() {
//        return data;
//    }
//
//    public ReadAvatarWorker(Map<String, PictureBitmapWorkerTask> taskMap,
//                      ImageView view, String url, FileLocationMethod method) {
//
//        this.globalContext = GlobalContext.getInstance();
//        this.lruCache = globalContext.getAvatarCache();
//        this.taskMap = taskMap;
//        this.viewList.add(new WeakReference<ImageView>(view));
//        this.data = url;
//        this.method = method;
//
//    }
//
//
//    @Override
//    protected Bitmap doInBackground(String... url) {
//
//        if (isCancelled())
//            return null;
//        String path = FileManager.getFilePathFromUrl(data, method);
//
//        TaskCache.waitForPictureDownload(data, path, method);
//
//        int height = 0;
//        int width = 0;
//
//        switch (method) {
//            case avatar_large:
//                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
//                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);
//                break;
//            case avatar_small:
//                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
//                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);
//                break;
//
//            case picture_thumbnail:
//                width = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_width);
//                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_height);
//                break;
//
//            case picture_large:
//                DisplayMetrics metrics = globalContext.getDisplayMetrics();
//
//                float reSize = globalContext.getResources().getDisplayMetrics().density;
//
//                height = globalContext.getResources().getDimensionPixelSize(R.dimen.timeline_pic_high_thumbnail_height);
//                //8 is  layout padding
//                width = (int) (metrics.widthPixels - (8 + 8) * reSize);
//        }
//
//        try {
//
//            return ImageTool.getRoundedCornerPic(path, width, height);
//        } catch (WeiboException failedBecauseOfNetwork) {
//            this.failedBecauseOfNetwork = failedBecauseOfNetwork;
//            cancel(true);
//        }
//        return null;
//
//    }
//
//    @Override
//    protected void onCancelled(Bitmap bitmap) {
//        for (WeakReference<ImageView> view : viewList) {
//            ImageView imageView = view.get();
//            if (imageView != null) {
//                if (canDisplay(imageView)) {
//                    imageView.setImageDrawable(new ColorDrawable(this.failedBecauseOfNetwork != null ? DebugColor.DOWNLOAD_FAILED : DebugColor.DOWNLOAD_CANCEL));
//                }
//            }
//
//        }
//        clean();
//        super.onCancelled(bitmap);
//    }
//
//    @Override
//    protected void onPostExecute(Bitmap bitmap) {
//
//        super.onPostExecute(bitmap);
//        displayBitmap(bitmap);
//        clean();
//    }
//
//    private void displayBitmap(Bitmap bitmap) {
//        for (WeakReference<ImageView> view : viewList) {
//            ImageView imageView = view.get();
//            if (imageView != null) {
//                if (canDisplay(imageView)) {
//                    if (bitmap != null) {
//                        playImageViewAnimation(imageView, bitmap);
//                        lruCache.put(data, bitmap);
//                    } else {
//                        imageView.setImageDrawable(new ColorDrawable(DebugColor.PICTURE_ERROR));
//                    }
//                }
//            }
//
//        }
//    }
//
//    private boolean canDisplay(ImageView view) {
//        if (view != null) {
//            IPictureWorker bitmapDownloaderTask = getBitmapDownloaderTask(view);
//            if (this == bitmapDownloaderTask) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static IPictureWorker getBitmapDownloaderTask(ImageView imageView) {
//        if (imageView != null) {
//            Drawable drawable = imageView.getDrawable();
//            if (drawable instanceof PictureBitmapDrawable) {
//                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
//                return downloadedDrawable.getBitmapDownloaderTask();
//            }
//        }
//        return null;
//    }
//
//    private void playImageViewAnimation(final ImageView view, final Bitmap bitmap) {
//        final Animation anim_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.timeline_pic_fade_out);
//        final Animation anim_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.timeline_pic_fade_in);
//
//        anim_out.setAnimationListener(new Animation.AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//                anim_in.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//                    }
//
//                    //clear animation avoid memory leak
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        if (view.getAnimation() != null && view.getAnimation().hasEnded()) {
//                            view.clearAnimation();
//                        }
//                    }
//                });
//
//                if (canDisplay(view)) {
//                    view.setImageBitmap(bitmap);
//                    view.startAnimation(anim_in);
//                }
//            }
//        });
//        if (view.getAnimation() == null || view.getAnimation().hasEnded())
//            view.startAnimation(anim_out);
//    }
//
//    private void clean() {
//        if (taskMap != null && taskMap.get(data) != null) {
//            taskMap.remove(data);
//        }
//        viewList.clear();
//        taskMap = null;
//        lruCache = null;
//        globalContext = null;
//    }
//
//}
