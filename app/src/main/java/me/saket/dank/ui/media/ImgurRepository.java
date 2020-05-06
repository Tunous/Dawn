package me.saket.dank.ui.media;

import android.app.Application;

import androidx.annotation.CheckResult;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import me.saket.dank.data.FileUploadProgressEvent;
import me.saket.dank.data.exceptions.InvalidImgurAlbumException;
import me.saket.dank.di.DankApi;
import me.saket.dank.urlparser.ImgurAlbumUnresolvedLink;
import me.saket.dank.utils.okhttp.OkHttpRequestBodyWithProgress;
import me.saket.dank.utils.okhttp.OkHttpRequestWriteProgressListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * TODO: Tests.
 */
@Singleton
public class ImgurRepository {

  private final DankApi dankApi;

  @Inject
  public ImgurRepository(Application _appContext, DankApi dankApi) {
    this.dankApi = dankApi;
  }

  /**
   * @throws InvalidImgurAlbumException               If an invalid Imgur link was found. Right now this happens only when no images are
   *                                                  returned by Imgur.
   */
  public Single<ImgurResponse> gallery(ImgurAlbumUnresolvedLink imgurAlbumUnresolvedLink) {
    return dankApi.imgurAlbum(imgurAlbumUnresolvedLink.albumId())
        .map(throwIfHttpError())
        .map(Response::body)
        .onErrorResumeNext(error -> {
          // Api returns a 404 when it was a single image and not an album.
          if (error instanceof HttpException && ((HttpException) error).code() == 404) {
            return Single.just(ImgurAlbumResponse.createEmpty());
          } else {
            return Single.error(error);
          }
        })
        .flatMap(albumResponse -> {
          if (albumResponse.hasImages()) {
            return Single.just(albumResponse);

          } else {
            // Okay, let's check if it was a single image.
            return dankApi.imgurImage(imgurAlbumUnresolvedLink.albumId())
                .map(Response::body);
          }
        })
        .doOnSuccess(albumResponse -> {
          if (!albumResponse.hasImages()) {
            throw new InvalidImgurAlbumException();
          }
        });
  }

  @CheckResult
  public Observable<FileUploadProgressEvent<ImgurUploadResponse>> uploadImage(File imageFile, String mimeType) {
    Relay<Float> uploadProgressStream = BehaviorRelay.createDefault(0f);

    RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), imageFile);
    OkHttpRequestWriteProgressListener uploadProgressListener = (bytesRead, totalBytes) -> {
      float progress = (float) bytesRead / totalBytes;
      uploadProgressStream.accept(progress);
    };
    RequestBody requestBodyWithProgress = OkHttpRequestBodyWithProgress.wrap(requestBody, uploadProgressListener);
    MultipartBody.Part multipartBodyPart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBodyWithProgress);

    Observable<FileUploadProgressEvent<ImgurUploadResponse>> uploadStream = dankApi.uploadToImgur(multipartBodyPart, "file")
        .map(throwIfHttpError())
        .map(Response::body)
        .map(FileUploadProgressEvent::createUploaded)
        .toObservable();

    return uploadProgressStream
        .map(FileUploadProgressEvent::<ImgurUploadResponse>createInFlight)
        .mergeWith(uploadStream);
  }

  private <T> Function<Response<T>, Response<T>> throwIfHttpError() {
    return response -> {
      if (response.isSuccessful()) {
        return response;
      } else {
        throw new HttpException(response);
      }
    };
  }

}
