% Cidon Estimate Quality

clear all;
close all;
clc;

grid on;
hold on;

if(matlabpool('size') == 0)
    maxNumCompThreads(2);
    matlabpool local 2;
end;


tol=1e-10;
%theta=0.99 %hard coded
%fb=inline('binocdf(k.*n.*p,n,p)-binocdf(n.*p./k,n,p)-0.99','k','n','p');
fb=inline('binocdf(floor(k.*n.*p),n,p)-binocdf(ceil(n.*p./k),n,p)-0.99','k','n','p');


batchsizes=[100,1000,10000];
%batchsizes=1000;
accuracy=[1.02:0.02:2];
%accuracy=1.001
minp=zeros(length(batchsizes),length(accuracy));
tic;
for i=1:length(batchsizes)
    n=batchsizes(i); % batch size
    parfor ii=1:length(accuracy)
        k=accuracy(ii);
        minp(i,ii)=bisect(2^-32,1-2^-32,fb,k,n,tol);
    end
end
toc;

set(gca, 'Box', 'off' ); % here gca means get current axis


h1=semilogy([1,accuracy],[1,minp(1,:)],'black');
h2=semilogy([1,accuracy],[1,minp(2,:)],'black');
h3=semilogy([1,accuracy],[1,minp(3,:)],'black');

legend('$n=100$','$n=1000$','$n=10000$');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);


hx=xlabel('$k$','Interpreter','Latex');
hy=ylabel('$p_\epsilon$','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);



fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(h1, 'LineStyle', 'none', 'LineWidth', 1.0, 'Color', 'Black');
set(h1, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h2, 'LineStyle', 'none', 'LineWidth', 1.0, 'Color',' Black');
set(h2, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h3, 'LineStyle', 'none', 'LineWidth', 1.0, 'Color',' Black');
set(h3, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

set(gca,'XTick',1:0.1:2);

saveas(fh, 'cidon-k-p-minimum', 'epsc');



C=2.623;
expectedT=zeros(length(batchsizes),length(accuracy));
for i=1:length(batchsizes)
    n=batchsizes(i); % batch size
    expectedT(i,:)=C.*minp(i,:).*n+1;
end

figure;

hold on;

h1=semilogy(accuracy,expectedT(1,:),'black');
h2=semilogy(accuracy,expectedT(2,:),'black');
h3=semilogy(accuracy,expectedT(3,:),'black');


legend('$n=100$','$n=1000$','$n=10000$');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);


hx=xlabel('$k$','Interpreter','Latex');
hy=ylabel('$L_{np_\epsilon}$','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);



fh = figure(2); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(h1, 'LineStyle', 'none', 'LineWidth', 1.0, 'Color', 'Black');
set(h1, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h2, 'LineStyle', 'none', 'LineWidth', 1.0, 'Color',' Black');
set(h2, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h3, 'LineStyle', 'none', 'LineWidth', 1.0, 'Color',' Black');
set(h3, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

set(gca,'XTick',1:0.1:2);
set(gca,'YScale','log');

grid on;

saveas(fh, 'cidon-k-L-minimum', 'epsc');
