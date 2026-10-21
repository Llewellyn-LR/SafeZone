package com.safezone.app.ui.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.WantedPerson;
import com.safezone.app.service.DataListener;
import com.safezone.app.service.FirebaseWantedPersonService;
import com.safezone.app.service.Subscription;
import com.safezone.app.ui.common.StreamErrorView;
import com.safezone.app.ui.common.Ui;

import java.util.ArrayList;
import java.util.List;

public class WantedPersonsAdminFragment extends Fragment {
    private FirebaseWantedPersonService service;
    private Subscription subscription;
    private View progress, empty;
    private StreamErrorView errorView;
    private RecyclerView recycler;
    private final Adapter adapter = new Adapter();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_wanted_admin, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        service = ServiceLocator.firebaseWantedPersons(requireContext());

        MaterialToolbar tb = v.findViewById(R.id.toolbar);

        tb.setOnMenuItemClickListener(item -> {
            FirebaseAuth.getInstance().signOut();
            return true;
        });
        v.findViewById(R.id.addFab).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), AddWantedPersonActivity.class)));

        progress = v.findViewById(R.id.progress);
        empty = v.findViewById(R.id.empty);
        errorView = v.findViewById(R.id.errorView);

        errorView.setMessage("Couldn't load wanted persons. Check the Firestore rules "
                + "(FIREBASE_SETUP.md section 6) and your connection.");
        recycler = v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        subscription = service.watchWantedPersons(new DataListener<List<WantedPerson>>() {
            @Override public void onData(List<WantedPerson> people) {
                progress.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                adapter.submit(people);
                empty.setVisibility(people.isEmpty() ? View.VISIBLE : View.GONE);
                recycler.setVisibility(people.isEmpty() ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                recycler.setVisibility(View.GONE);
                empty.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (subscription != null) subscription.cancel();
        super.onDestroyView();
    }

    private void confirmDelete(WantedPerson person) {
        androidx.appcompat.app.AlertDialog d = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove this entry?")
                .setMessage("This takes \"" + person.name + "\" off the public list immediately. "
                        + "This can't be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dlg, w) -> delete(person))
                .create();
        d.show();
        d.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.coral_text));
    }

    private void delete(WantedPerson person) {
        View root = requireView();
        service.deleteWantedPerson(person.id)
                .addOnSuccessListener(x -> Snackbar.make(root, "Removed.", Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Snackbar.make(root,
                        "Couldn't remove: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private final class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<WantedPerson> items = new ArrayList<>();

        void submit(List<WantedPerson> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_admin_wanted, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            WantedPerson p = items.get(pos);
            int coral = ContextCompat.getColor(h.itemView.getContext(), R.color.coral_text);
            int secondary = ContextCompat.getColor(h.itemView.getContext(), R.color.text_secondary);
            int navy = ContextCompat.getColor(h.itemView.getContext(), R.color.navy);

            h.title.setText(p.displayName());
            h.subtitle.setText(p.lastSeenArea + (p.armedAndDangerous ? " · Armed and dangerous" : ""));
            h.subtitle.setTextColor(p.armedAndDangerous ? coral : secondary);
            h.subtitle.setTypeface(null, p.armedAndDangerous
                    ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            h.avatar.setBackground(Ui.circle(Ui.withAlpha(navy, 0.1f)));
            bindAvatar(h.avatar, p.photoUrl, navy);
            h.delete.setOnClickListener(x -> confirmDelete(p));
        }

        private void bindAvatar(ImageView iv, @Nullable String url, int navy) {
            Runnable placeholder = () -> {
                iv.setScaleType(ImageView.ScaleType.CENTER);
                iv.setImageResource(R.drawable.ic_person);
                iv.setImageTintList(ColorStateList.valueOf(navy));
            };
            if (url == null) { placeholder.run(); return; }
            iv.setImageTintList(null);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(iv).load(url).circleCrop().listener(new RequestListener<Drawable>() {
                @Override public boolean onLoadFailed(@Nullable GlideException e, Object m,
                                                      Target<Drawable> t, boolean f) {
                    placeholder.run();
                    return true;
                }
                @Override public boolean onResourceReady(Drawable r, Object m, Target<Drawable> t,
                                                         DataSource d, boolean f) { return false; }
            }).into(iv);
        }

        @Override public int getItemCount() { return items.size(); }

        final class VH extends RecyclerView.ViewHolder {
            final ImageView avatar, delete;
            final TextView title, subtitle;
            VH(@NonNull View v) {
                super(v);
                avatar = v.findViewById(R.id.avatar);
                delete = v.findViewById(R.id.delete);
                title = v.findViewById(R.id.title);
                subtitle = v.findViewById(R.id.subtitle);
            }
        }
    }
}
