package org.wikipedia.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.wikipedia.dataclient.WikiSite;
import org.wikipedia.dataclient.restbase.RbRelatedPages;
import org.wikipedia.dataclient.restbase.page.RbPageSummary;
import org.wikipedia.dataclient.retrofit.RbCachedService;
import org.wikipedia.dataclient.retrofit.WikiCachedService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class RelatedPagesSearchClient {
    @NonNull private WikiCachedService<Service> cachedService = new RbCachedService<>(Service.class);

    public interface Callback {
        void success(@NonNull Call<RbRelatedPages> call, @Nullable List<RbPageSummary> results);
        void failure(@NonNull Call<RbRelatedPages> call, @NonNull Throwable caught);
    }

    public Call<RbRelatedPages> request(@NonNull String title, @NonNull WikiSite wiki, int limit, @NonNull Callback cb) {
        return request(cachedService.service(wiki), title, limit, cb);
    }

    @VisibleForTesting
    Call<RbRelatedPages> request(@NonNull Service service, @NonNull String title, int limit, @NonNull Callback cb) {

        Call<RbRelatedPages> call = service.fetch(title);

        call.enqueue(new retrofit2.Callback<RbRelatedPages>() {
            @Override public void onResponse(@NonNull Call<RbRelatedPages> call,
                                             @NonNull Response<RbRelatedPages> response) {
                if (response.body() != null && response.body().getPages() != null) {
                    // noinspection ConstantConditions
                    cb.success(call, limit < 0 ? response.body().getPages() : response.body().getPages(limit));
                } else {
                    cb.failure(call, new IOException("An unknown error occurred."));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RbRelatedPages> call, @NonNull Throwable t) {
                cb.failure(call, t);
            }
        });
        return call;
    }

    @VisibleForTesting
    interface Service {
        @NonNull
        @GET("page/related/{title}")
        Call<RbRelatedPages> fetch(@Path("title") String title);
    }
}
